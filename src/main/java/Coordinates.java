public class Coordinates {
    private double x;
    private double y;
    //coordinates constructor takes in the double x and y values from coordinate objects and sets them to the global vars which are then returned in the getX and getY methods when needed
    public Coordinates(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
