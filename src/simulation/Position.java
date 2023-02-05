package simulation;

public class Position implements Cloneable{
	public Space space;
    public int x;
    public int y;
    public double z;
    double pheromone;
    public long lasttime;

    public Object clone(){//クローンを作ることによって参照渡しによる値の書き換えを防ぐことができる
        try{
            return super.clone();//Cloneableに入っているcloneという関数を利用している
        }catch (CloneNotSupportedException e){
            throw new InternalError(e.toString());
        }
    }
    
    //演算子オーバーライド
    public boolean equals(Object o){
        if(o instanceof Position){
            Position p = (Position) o;
            return p.x == x && p.y == y;
        }
        return false;
    }
    
    //コンストラクタ
    public Position(int x, int y,double z, Space space) {
        this.x = x;
        this.y = y;
        this.z = z;
        
        //フェロモンの初期化
        
        if(z<1) {
        	this.pheromone = 1.0;
        }
        //高さに対してどのようにフェロモンを塗布するか、肝
        else {
        	this.pheromone=z*10;
        }
        //フィールド共有
        this.space = space;
    }

    public String position2string(Position pos) {
        String str;
        str = "(x, y)" + pos.x + "," + pos.y;
        return str;
    }
    
    public  Position up() {
        return space.position(x, y - 1);
    }
    
    public Position down() {
        return space.position(x, y + 1);
    }
    public Position left() {
        return space.position(x - 1, y);
    }
    public Position right() {
        return space.position(x + 1, y);
    }
    public Position up_right() {
        return space.position(x + 1, y - 1);
    }
    public Position up_left() {
        return space.position(x - 1, y - 1);
    }
    public Position down_right() {
        return space.position(x + 1, y + 1);
    }
    public Position down_left() {
        return space.position(x - 1, y + 1);
    }

    public Position upnum(int num) {
        return space.position(x, y - num);
    }
    public Position downnum(int num) {
        return space.position(x, y + num);
    }
    public Position leftnum(int num) {
        return space.position(x - num, y);
    }
    public Position rightnum(int num) {
        return space.position(x + num, y);
    }
    public Position up_rightnum(int num) {
        return space.position(x + num, y - num);
    }
    public Position up_leftnum(int num) {
        return space.position(x - num, y - num);
    }
    public Position down_rightnum(int num) {
        return space.position(x + num, y + num);
    }
    public Position down_leftnum(int num) {
        return space.position(x - num, y + num);
    }

    public Position getPosition(int _x, int _y) {
        return space.position(x + _x, y + _y);
    }

    public String toString() {
        return Float.toString((float) getPheromone());
    }

    public double doReduce() {
        if (pheromone > space.MMAS_minPheromone) {
            pheromone = (1 - space.constPheromone) * (this.pheromone);
        }
        return space.MMAS_minPheromone;
    }

    public double getRawPheromone() {
        return pheromone;
    }

    public double getPheromone() {
        //double tmp = pheromone-((double)space.time-lasttime)*(space.constPheromone);
        double tmp = pheromone;
        //Min - Max Ant System
        if (tmp < -50.00) {
            return 0;
        } else if (tmp < space.MMAS_minPheromone) {
            return space.MMAS_minPheromone;
        } else if (tmp > space.MMAS_maxPheromone) {
            return space.MMAS_maxPheromone;
        } else {
            return tmp;
        }
    }
    
    //フェロモン塗布
    public void addPheromone(double pheromone) {
        //複合フェロモンを見て塗られて居なければその場所に塗布
        if (getRawPheromone() > -50.00) {
            this.pheromone = getPheromone() + (pheromone);
        }

        lasttime = space.time;
        if (space.maxpheromone.getPheromone() < getPheromone())
            space.maxpheromone = this;
    }

    public void reducePheromone(double pheromone) {
        this.pheromone = this.pheromone - pheromone;
    }

    public void reducePheromoneByRatio(double ratio) {
        this.pheromone = this.pheromone * ratio;
    }

}
