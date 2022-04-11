import java.util.*;

public class SIC {
    static public Map<String, String> OPTAB = new HashMap<String, String>()
    {
        {
            // SIC Instruction Set
            put("ADD", "0x18");
            put("AND","0x40");
            put("COMP","0x28");
            put("DIV","0x24");
            put("J","0x3C");
            put("JEQ","0x30");
            put("JGT","0x34");
            put("JLT","0x38");
            put("JSUB","0x48");
            put("LDA","0x00");
            put("LDCH","0x50");
            put("LDL","0x08");
            put("LDX","0x04");
            put("MUL","0x20");
            put("OR","0x44");
            put("RD","0xD8");
            put("RSUB","0x4C");
            put("STA","0x0C");
            put("STCH","0x54");
            put("STL","0x14");
            put("STSW","0xE8");
            put("STX","0x10");
            put("SUB","0x1C");
            put("TD","0xE0");
            put("TIX","0x2C");
            put("WD","0xDC");
        }};

    static public List<String> DIRECTIVE = new ArrayList<String>(Arrays.asList("START", "END", "WORD", "BYTE", "RESW", "RESB"));

    static public SICCode assemblyCodeParser(String line){
        // input is an instruction
        if(line.length() > 0){
            // the first character is '.' or '\n' => invalid instruction or directive
            if(line.charAt(0) == '.' || line.charAt(0) == '\n'){
                return new SICCode();
            }
        }
        // split the instruction to tokens
        String[] tokens = line.split("\\s+");
        /*System.out.println("tokens: ");
        for(int i = 0; i < tokens.length; i++){
            System.out.println(tokens[i]);
        }*/

        for(int i = 0; i < tokens.length; i++){
            if(tokens[i].equals("")){
                tokens[i] = "None";
            }
        }
        /*if(tokens[0].equals("")){
            if(tokens.length == 2){
                tokens = new String[] { tokens[1], "None" };
            }
            else{
                tokens = new String[] { tokens[1], tokens[2] };
            }

        }*/

        // having only one token
        if(tokens.length == 1){
            // the first token is neither opcode nor directive
            if(!isOpcodeOrDirective(tokens[0])){
                System.out.println("Your assembly code is wrong!!(1)");
                return new SICCode();
            }
            // valid instruction
            return new SICCode("None", tokens[0], "None");
        }

        // having two tokens
        else if(tokens.length == 2){

            // the first token is opcode or directive
            if(isOpcodeOrDirective(tokens[0])){
                return new SICCode("None", tokens[0], tokens[1]);
            }

            // the second token is opcode or directive
            else if(isOpcodeOrDirective(tokens[1])){
                return new SICCode(tokens[0], tokens[1], "None");
            }

            // having no opcode or directive
            else{
                System.out.println("Your assembly code is wrong!!(2)");
                return new SICCode();
            }
        }

        // having three tokens
        else if(tokens.length == 3){
            /************************************************************************************/
            /* The second token of an instruction with three token must be opcode or directive. */
            /* e.g: FIRST LDA ONE                                                                */
            /************************************************************************************/

            // the second token is opcode or directive
            if(isOpcodeOrDirective(tokens[1])){
                return new SICCode(tokens[0], tokens[1], tokens[2]);
            }

            // something wrong in your assembly code
            else{
                System.out.println("Your assembly code is wrong!!(3)");
                return new SICCode();
            }
        }

        // having more than three tokens or having no token
        else{
            System.out.println("Your assembly code code is wrong!!(4)");
            return new SICCode();
        }
    }

    // check if the token is opcode or directive or neither
    static public boolean isOpcodeOrDirective(String token){
        if(SIC.isInstruction(token)){
            return true;
        }
        if(SIC.isDirective(token)){
            return true;
        }
        return false;

    }

    static public boolean isDirective(String token){
        boolean flag = false;
        for(int i = 0; i < DIRECTIVE.size(); i++){
            if(token.equals(DIRECTIVE.get(i))){
                flag = true;
                break;
            }
        }
        return flag;
    }

    static public boolean isInstruction(String token){
        boolean flag = false;
        if(OPTAB.containsKey(token)){
            flag = true;
        }
        return flag;
    }
}
