package affichage.EndScreen;

public class EndScreenData {
    private final StringBuilder name = new StringBuilder();
    private boolean finished = false;

    public void addChar(char c) { name.append(c); }
    public void removeLastChar() { if (name.length() > 0) name.deleteCharAt(name.length() - 1); }
    public String getName() { return name.toString(); }
    public void finish() { finished = true; }
    public boolean isFinished() { return finished; }
}