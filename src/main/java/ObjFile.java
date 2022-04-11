import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ObjFile {
    static private PrintWriter writer;

    static public void writeHeader(File objFile, String programName, int startingAddr, int programLen){
        // add "H"
        String header = "H";

        // add program name to header
        header += extendProgramName(programName);

        // add starting address to header
        header += extendHexStr(Integer.toHexString(startingAddr));

        // add program length to header
        header += extendHexStr(Integer.toHexString(programLen));

        // add newline characters to header
        header += "\n";

        // write header to obj file
        try{
            writer = new PrintWriter(objFile);
            writer.write(header);
        }
        catch (FileNotFoundException e){
            System.out.println("file not found!!");
        }

    }

    static public void writeText(File objFile, int starting, String tempText){
        // add "T"
        String textRecord = "T";

        // add starting address to textRecord
        textRecord += extendHexStr(Integer.toHexString(starting));

        // get length of object code int this record in bytes
        System.out.println("Write tempText: " + tempText);
        String recordLen = Integer.toHexString(tempText.length()/2);

        // extend to 2 bytes with '0'
        for(int i = 0; i < 2 - recordLen.length(); i++){
            recordLen = '0' + recordLen;
        }

        // transform recordLen to upper case
        recordLen = recordLen.toUpperCase();

        // add l to textRecord
        textRecord += recordLen;

        // add tempText to textRecord
        textRecord += tempText;
        textRecord += "\n";

        // write textRecord to obj file
        writer.write(textRecord);
    }

    static public void writeEnd(File objFile, int address){
        // add "E"
        String endRecord = "E";

        // aad address to endRecord
        endRecord += extendHexStr(Integer.toHexString(address));

        // write endRecord to obj file
        writer.write(endRecord);
        writer.close();

    }
    static public String extendProgramName(String programName){
        // extend to 6 bytes with space
        int n = 6 - programName.length();
        for(int i = 0; i < n; i++){
            programName = programName + ' ';
        }
        return programName;
    }

    static public String extendHexStr(String hexStr){

        // remove the first and second bit(e.g 0x12 or just 12)
        if(hexStr.length() >=2 && hexStr.substring(0,2).equals("0x")){
            hexStr = hexStr.substring(2);
        }

        // extend to 6 bytes with '0'
        int n = 6 - hexStr.length();
        for(int i = 0; i < n; i++){
            hexStr = '0' + hexStr;
        }

        // transform hexStr to upper case
        hexStr = hexStr.toUpperCase();

        return hexStr;
    }
}
