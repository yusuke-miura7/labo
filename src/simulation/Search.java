package simulation;

public class Search {
	
    //初期配置可能な座標情報を返す
    public Position generatePosition(Space space, int world_x, int world_y,Position[] wall, Position[] building,Position[] safe, Position[] dangerArea) {
        Position candidate;
        while (true) {
            int x, y;
            x = (int) (Math.random() * world_x);
            y = (int) (Math.random() * world_y);
            candidate = space.position(x, y);
            if (isObject(wall, building ,safe,dangerArea,candidate)) break;
        }
        return candidate;
    }
    
    //火を生成可能な座標情報を返す
    public Position generateFirePosition(Space space, int world_x, int world_y, Position[] wall, Position[] building, Position[] safe, Position[] danger) {
        Position tmp;
        while (true) {
            int x, y;
            x = (int) (Math.random() * world_x);
            y = (int) (Math.random() * world_y);
            tmp = space.position(x, y);
            if (isRoad(wall, building, safe, danger, tmp) && isCreateFire(x, y, 2)) break;
        }
        return tmp;
    }
	
    //引数に渡した２つのPositionが同じPositionかどうか判断する、同じであればtrue
    public boolean isSamePoint(Position pre, Position cur) {
        return !(pre == null || cur == null) && ((pre.x == cur.x) && (pre.y == cur.y));
    }

    //配置しようとした場所にObjectがあるかどうか、なければtrue
    public boolean isObject(Position[] wall,Position[] building,Position[] safe, Position[] danger, Position gen) {
    	for (int i = 0; i < wall.length; i++) {
            if (isSamePoint(wall[i], gen)) return false;
        }
    	
    	for (int i = 0; i < building.length; i++) {
    		if (isSamePoint(building[i], gen)) return false;
    	}
    	
        for (int i = 0; i < danger.length; i++) {
            if (isSamePoint(danger[i], gen)) return false;
        }
        
        for (int i=0; i< safe.length; i++) {
        	if (isSamePoint(safe[i],gen)) return false;
        }

        return true;
    }

    boolean isCreateFire(int x, int y, int th){
        return (
                (x<(Core.world_x - th))
                &&
                (th<x)
                &&
                (y<(Core.world_y - th))
                &&
                (th<y)
        );
    }
    
    public boolean isRoad(Position[] wall, Position[] building, Position[] safe, Position[] danger,  Position gen) {

        for (int i = 0; i < danger.length; i++) {
            if (isSamePoint(danger[i], gen)) return false;
        }

        for (int i = 0; i < wall.length; i++) {
            if (isSamePoint(wall[i], gen)) return false;
        }

        for (int i = 0; i < safe.length; i++) {
            if (isSamePoint(safe[i], gen)) return false;
        }
        
        for (int i = 0; i < building.length; i++) {
        	if (isSamePoint(building[i], gen)) return false;
        }

        return true;
    }
    
}

