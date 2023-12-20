package setting;

public class WumpusPercept {
    private boolean isStench;
    private boolean isBreeze;
    private boolean isGlitter;
    private boolean isScream;

    public void setStench() {
        isStench = true;
    }

    public void setBreeze() {
        isBreeze = true;
    }

    public void setGlitter() {
        isGlitter = true;
    }

    public void setScream() {
        isScream = true;
    }

    public boolean isStench() {
        return isStench;
    }

    public boolean isBreeze() {
        return isBreeze;
    }

    public boolean isGlitter() {
        return isGlitter;
    }

    public boolean isScream() {
        return isScream;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        var template = "There is";

        if (isStench)
            result.append(template).append(" Stench. ");

        if (isBreeze)
            result.append(template).append("There is Breeze. ");

        if (isGlitter)
            result.append(template).append("There is Glitter. ");

        if (isScream)
            result.append(template).append("There is Scream. ");

        return result.toString();
    }
}