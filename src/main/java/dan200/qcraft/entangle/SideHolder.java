package dan200.qcraft.entangle;

public class SideHolder {
    private volatile short side;
    SideHolder(short side) {
        this.side = side;
    }

    public short getSide() {
        return side;
    }

    public void setSide(short side) {
        this.side = side;
    }
}
