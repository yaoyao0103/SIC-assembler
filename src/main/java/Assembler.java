import javax.crypto.Mac;
import java.io.*;
import java.util.*;

public class Assembler {
    private List<String> lines = new ArrayList<String>();
    private Map<String, Integer> SYMTAB = new HashMap<String, Integer>();
    private int startingAddr = 0;
    private int locationCnt = 0;
    private int programLen = 0;
    private int operandLen = 0;
    private SICCode code;
    private String first = "";
    private String second = "";
    private String third = "";


    public void execute(int x){
        // get assembly code from file
        try{
            File doc = new File("D:\\SIC\\src\\main\\resources\\example" + x + ".asm");
            BufferedReader obj = new BufferedReader(new FileReader(doc));

            String str;
            while ((str = obj.readLine()) != null)
                lines.add(str);
            System.out.println("lines: " + lines);
        }
        catch (Exception e){
            System.out.println(e);
        }
        /******************** pass 1 **************************/

        System.out.println("****************** Pass 1 ********************");
        for(String line : lines){
            code = SIC.assemblyCodeParser(line);
            first = code.getFirst();
            second = code.getSecond();
            third = code.getThird();

            System.out.println("Assembly Code: " + first + " " + second + " " + third);
            // your SIC code is invalid
            if(code.isInvalid()){
                // skip
                continue;
            }

            /******************** start & end ************************/
            // if the second token is "START" => initialize & start
            if(second.equals("START")){
                // the third token is an address
                // initialize starting address and location counter
                startingAddr = Integer.parseInt(third, 16);
                locationCnt = startingAddr;
            }

            // if the second token is "END" => calculate program length & end
            else if(second.equals("END")){
                // calculate the program length and break
                programLen = locationCnt - startingAddr;
                break;
            }
            ///////////////////////////////////////////////////////////////

            /****************** handle symbol table ********************/
            // having label
            if(first != "None"){

                // The label already exists in symbol table => duplicated label
                if(SYMTAB.containsKey(first)){
                    System.out.println("Duplicated label: " + first + "!!!");
                    return;
                }
                // else => put the label to the symbol table
                else{
                    SYMTAB.put(first, locationCnt);
                }
            }
            ///////////////////////////////////////////////////////////////

            /****************** handle location counter *****************/
            // is an instruction => location counter += 3
            if(SIC.isInstruction(second)){
                locationCnt += 3;
            }
            
            // is a directive
            else if(SIC.isDirective(second)){
                // is WORD(assembler directives) => location counter += 3
                if(second.equals("WORD")){
                    locationCnt += 3;
                }
                // is RESW(assembler directives) => location counter += word*3 (bytes)
                else if(second.equals("RESW")){
                    // transform to integer and calculate the number of bytes (word = 3bytes)
                    locationCnt += Integer.parseInt(third) * 3;
                }
                // is RESB(assembler directives) => location counter += bytes
                else if(second.equals("RESB")){
                    locationCnt += Integer.parseInt(third);
                }
                // is BYTE(assembler directives)
                else if(second.equals("BYTE")){
                    // char
                    if(third.charAt(0) == 'C'){
                        // deduct the length of "'C'"
                        locationCnt += (third.length() - 3);
                    }
                    // hex
                    else if(third.charAt(0) == 'X'){
                        // deduct the length of "'X'" and calculate the number of bytes
                        locationCnt += (third.length() - 3)/2;
                    }
                    else{
                        System.out.println("Assembly Code: " + first + " " + second + " " + third + " has a wrong format on the third token!!!");
                        return;
                    }
                }
                else{
                    System.out.println("Skip this directive");
                    continue;
                }
            }

            else{
                System.out.println("Neither instruction nor directive");
                continue;
            }

        }

        System.out.println("Symbol Table: " + SYMTAB);

        /******************** pass 2 **************************/
        System.out.println("****************** Pass 2 ********************");
        try{
            File objFile = new File("D:\\SIC\\src\\main\\resources\\example" + x +".obj");
            Boolean reserveFlag = false;

            /**************** Write header **********************/
            code = SIC.assemblyCodeParser(lines.get(0));
            first = code.getFirst();
            second = code.getSecond();
            third = code.getThird();
            
            // initialize
            locationCnt = 0;
            String programName = "";

            // if the second token is "START"
            if(second.equals("START")){
                // get program name & set set location counter
                locationCnt = Integer.parseInt(third, 16);
                programName = first;
            }

            // set starting address
            startingAddr = locationCnt;

            ObjFile.writeHeader(objFile, programName, startingAddr, programLen);
            //////////////////////////////////////////////////////////////

            /**************** Write textRecord & endRecord **********************/

            // a line of textRecord
            String tempText = "";

            // initialize textRecord starting address
            int textStartingAddr = locationCnt;

            for(String line:lines){
                code = SIC.assemblyCodeParser(line);
                first = code.getFirst();
                second = code.getSecond();
                third = code.getThird();

                System.out.println("Assembly Code: " + first + " " + second +" "+ third);
                // if instruction or directive is invalid
                if(code.isInvalid()){
                    System.out.println("Invalid instruction or directive");
                    continue;
                }

                /******************* handle the start and end of textRecord  *************/

                // if opcode is "START"
                if(second.equals("START")){
                    // skip
                    continue;
                }

                // if opcode is "END"
                else if(second.equals("END")){
                    /********************* write textRecord ***************/
                    // exist textRecord
                    if(tempText.length() > 0){
                        // write tempText to textRecord
                        ObjFile.writeText(objFile, textStartingAddr, tempText);
                    }
                    ////////////////////////////////////////////////////////

                    /********************** write endRecord *****************/
                    // calculate program length
                    programLen = locationCnt - startingAddr;
                    int address = startingAddr;

                    // get data from symbol table
                    if(third != "None"){
                        address = SYMTAB.get(third);
                    }

                    // write endRecord
                    ObjFile.writeEnd(objFile, address);
                    break;
                    //////////////////////////////////////////////////////////
                }
                ////////////////////////////////////////////////////////////////////

                /************************** handle the other condition ******************/
                else {
                    /************************ handle instruction **********************/
                    // if the opcode existed in SIC OP table(namely, it is an instruction)
                    if (SIC.OPTAB.containsKey(second)) {
                        // generate instruction
                        int tempInstruction = Integer.parseInt(SIC.OPTAB.get(second).substring(2),16) * (int)Math.pow(2,16);

                        if(!third.equals("None")){
                            // if operand tail is ",X"
                            if(third.substring(third.length()-2).equals(",X")){
                                // set 'x' bit to 1 (+2^15)
                                tempInstruction += (int)Math.pow(2,15);
                                // remove ",X"
                                third = third.substring(0,third.length()-2);
                            }
                            // set address field
                            if(SYMTAB.containsKey(third)){
                                tempInstruction += SYMTAB.get(third);
                            }
                            else{
                                System.out.println("Undefined Symbol: " + third + "!!!");
                                return;
                            }
                        }
                        String instruction =  ObjFile.extendHexStr(Integer.toHexString(tempInstruction));

                        // exceeding the limit of a line of tempText
                        if ((locationCnt + 3 - textStartingAddr > 30) || reserveFlag) {
                            // write current tempText to textRecord
                            ObjFile.writeText(objFile, textStartingAddr, tempText);

                            // initialize new line of tempText and put the instruction to textRecord
                            textStartingAddr = locationCnt;
                            tempText = instruction;
                        }
                        // not exceeding the limit
                        else {
                            // put the instruction to tempText directly
                            tempText += instruction;
                        }
                        reserveFlag = false;

                        // increase the location counter(3 bytes)
                        locationCnt += 3;
                    }
                    //////////////////////////////////////////////////////////////

                    /******************* handle directive ***********************/
                    // it is a WORD directive
                    else if (second.equals("WORD")) {
                        // get the constant
                        String constant = ObjFile.extendHexStr(Integer.toHexString(Integer.parseInt(third)));

                        // exceeding the limit of a line of tempText
                        if ((locationCnt + 3 - textStartingAddr > 30) || reserveFlag) {
                            // write current tempText to obj file
                            ObjFile.writeText(objFile, textStartingAddr, tempText);

                            // initialize new line of tempText and put the constant to textRecord
                            textStartingAddr = locationCnt;
                            tempText = constant;
                        }
                        // not exceeding the limit
                        else {
                            // put the constant to tempText directly
                            tempText += constant;
                        }
                        reserveFlag = false;

                        // increase the location counter(3 bytes)
                        locationCnt += 3;
                    }
                    // it is a BYTE directive
                    else if (second.equals("BYTE")) {
                        String constant = "";
                        if (third.charAt(0) == 'X') {
                            // deduct the length of "'X'" and calculate the number of bytes
                            operandLen = (third.length() - 3) / 2;
                            constant = third.substring(2, third.length() - 1);
                        }
                        else if (third.charAt(0) == 'C') {
                            // deduct the length of "'C'"
                            operandLen = third.length() - 3;
                            constant = "";
                            // from the third char to tail-1 (because operand format is " C'xxxx' ")
                            for(int i = 2; i < third.length()-1; i++){
                                constant += Integer.toHexString((int)(third.charAt(i)));
                            }
                            constant = constant.toUpperCase();
                        }
                        else{
                            System.out.println("Assembly Code: " + first + " " + second + " " + third + " has a wrong format on the third token!!!");
                            return;
                        }

                        // exceeding the limit of a line of tempText
                        if ((locationCnt + 3 - textStartingAddr > 30) || reserveFlag) {
                            // write current tempText to obj file
                            ObjFile.writeText(objFile, textStartingAddr, tempText);

                            // initialize new line of tempText and put the constant to textRecord
                            textStartingAddr = locationCnt;
                            tempText = constant;
                        }
                        // not exceeding the limit
                        else {
                            // put the constant to tempText directly
                            tempText += constant;
                        }
                        reserveFlag = false;

                        // increase the location counter(the length of operand*3 bytes)
                        locationCnt += operandLen;
                    }

                    // it is a RESB directive
                    else if (second.equals("RESB")) {
                        // increase the location counter
                        locationCnt += Integer.parseInt(third);
                        reserveFlag = true;
                    }

                    // it is a RESW directive
                    else if (second.equals("RESW")) {
                        // increase the location counter(1word = 3bytes)
                        locationCnt += Integer.parseInt(third) * 3;
                        reserveFlag = true;
                    }

                }
                System.out.println("Current tempText:" + tempText);
            }
        }
        catch (Exception e){
            System.out.println("file not found!!");
        }

    }
}
