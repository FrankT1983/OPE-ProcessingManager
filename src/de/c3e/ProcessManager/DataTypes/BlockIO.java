package de.c3e.ProcessManager.DataTypes;

/**
 * The Input or Output port of a Block.
 */
public class BlockIO
{
    public String Name;
    public String Id;       // need both for the icy stuff => from there design name has to be unique, they still introducesd an id, AND THEN LINK WITH THE ID, WHICH THE BLOCK DOES NOT KNOW WTF !!!!

    private boolean IsValid;
    private Object Value;

    public synchronized void SetValue(Object value)
    {
        this.Value = value;
        this.IsValid = true;
    }

    public synchronized void Invalidate()
    {
        this.Value = null;
        this.IsValid = false;
    }

    public Object getValue()
    {
        return this.Value;
    }

    public boolean isValid()
    {
        return this.IsValid;
    }

    public boolean NameOrIdEquals(String toTest)
    {
        return (this.Name.equals(toTest) || this.Id.equals(toTest));
    }
}
