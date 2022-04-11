public class SICCode {
    private String first;
    private String second;
    private String third;
    public SICCode(){
        first = "None";
        second = "None";
        third = "None";
    }
    public SICCode(String first, String second, String third){
        this.first = first;
        this.second = second;
        // remove "0x"
        this.third = (third.length() >= 2 && third.substring(0,2).equals("0x"))? third.substring(2):third;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        // remove "0x"
        this.third = (third.length() >= 2 && third.substring(0,2).equals("0x"))? third.substring(2):third;
    }

    public boolean isInvalid(){
        if(this.first.equals("None") && this.second.equals("None") && this.third.equals("None")){
            return true;
        }
        return false;
    }
}
