package simulation;

import java.util.ArrayList;
import java.util.HashMap;

public class Ant {
	Space space;
    Position position;
    Position[] wall;
    Position[] building;
    Position[] goal;
    Position[] dangerArea;
    Position[] fireArea;
    Ant[] anotherAgent;
    private double addPheromone;
    double proposalPheromone = 100.00; //消臭フェロモン量
    double alpha; //移動決定時のpheromone要因の割合
    
    //状態変数
    int isgoal = 0;
    int isdead = 0;
    int deth_fire = 0;
    int deth_tsunami = 0;
    int hitCount = 0;
    
    //これまで通った経路の座標情報を格納する配列
    Position[] prePosition = new Position[10];
    ArrayList<Position> path = new ArrayList<Position>(1);//ゴール時ここに格納された座標にフェロモンを塗布する
    
    //辞書(key:string(x,y) value:position(座標情報))
    HashMap<String, Position> path_hash = new HashMap<String, Position>(10000);

    public Ant(Space space,Position startPos,double add_pheromone, double al, Position[] wall,Position[] building, Position[] safe, Position[] dangerArea) {
        position = startPos;
        this.space = space;
        this.addPheromone = add_pheromone;
        this.alpha = al;
        this.isgoal = 0;
        this.wall = wall;
        this.building = building;
        this.goal = safe;
        this.dangerArea = dangerArea;
        //初期化
        for (int i = 0; i < prePosition.length; i++) {
            this.prePosition[i] = startPos;
        }
    }

    //2つのPositionが同じかどうか判定
    public boolean isSamePoint(Position pre, Position cur) {
        return !(pre == null || cur == null) && ((pre.x == cur.x) && (pre.y == cur.y));
    }

    public Position returnPos() {
        return position;
    }

    public Position[] returnPrePos() {
        return prePosition;
    }
    
    //移動先positionを返す
    private Position positionByInt(int d) {
        Position newPosition = null;
        if (d == 0) newPosition = position.up();
        if (d == 1) newPosition = position.up_left();
        if (d == 2) newPosition = position.left();
        if (d == 3) newPosition = position.down_left();
        if (d == 4) newPosition = position.down();
        if (d == 5) newPosition = position.down_right();
        if (d == 6) newPosition = position.right();
        if (d == 7) newPosition = position.up_right();
        return newPosition;
    }

    //各種障害物(壁、危険エリア、火災エリア)との衝突判定、ぶつかるとtrue
    private boolean check_collision(int d) {
        Position newPosition = positionByInt(d);

        for(Position a_wall : wall) {
            if (isSamePoint(newPosition, a_wall)) {
                return true;
            }
        }
        
        for(Position a_building: building) {
        	if(isSamePoint(newPosition,a_building)) {
        		return true;
        	}
        }

        for(Position a_danger_area : dangerArea) {
            if (isSamePoint(newPosition, a_danger_area)) {
                if (Core.isProposalMode) {
                    this.hitCount++;
                    newPosition.reducePheromone(proposalPheromone);
                } else {
                    this.hitCount++;
                }
                return true;
            }
        }

        for(Position a_fire_area : fireArea) {
            if (isSamePoint(newPosition, a_fire_area)) {
                if (Core.isProposalMode) {
                    this.hitCount++;
                    newPosition.reducePheromone(proposalPheromone);
                } else {
                    this.hitCount++;
                }
                return true;
            }
        }
        return false;
    }

    //消臭フェロモンの拡散を防ぐために壁との衝突判定を行う
    private boolean isStopDiffusion(Position pos) {
        if (pos == null) return true;

        for (Position aDangerArea : dangerArea) {
            if (isSamePoint(pos, aDangerArea)) {
                return true;
            }
        }
        for (Position aWall : wall) {
            if (isSamePoint(pos, aWall)) {
                return true;
            }
        }
        return false;
    }

    //ゴール(餌or巣)に到達できるなら移動しtrueを返す
    //それ以外は引数に従い移動する
    private boolean walkStep(int d) {
        for (Position aGoal : this.goal) {
        if (isSamePoint(position.up(), aGoal)) {
            position = position.up();
            return true;
        }
        if (isSamePoint(position.up_left(), aGoal)){
            position = position.up_left();
            return true;
        }
        if (isSamePoint(position.left(), aGoal)){
            position = position.left();
            return true;
        }
        if (isSamePoint(position.down_left(), aGoal)){
            position = position.down_left();
            return true;
        }
        if (isSamePoint(position.down(), aGoal)){
            position = position.down();
            return true;
        }
        if (isSamePoint(position.down_right(), aGoal)){
            position = position.down_right();
            return true;
        }
        if (isSamePoint(position.right(), aGoal)){
            position = position.right();
            return true;
        }
        }
        
        Position newPosition = positionByInt(d);

        if ((newPosition != null) && !check_collision(d)) {
            position = newPosition;
        }
        
        for (Position aGoal : this.goal) {
            if (isSamePoint(position, aGoal)) return true;
        }
        return false;

    }

    //八方向のフェロモンを配列に格納する
    private double[] smellPheromone() {
        double[] smell = new double[8];
        double sum = 0;

        for (int i = 0; i < 8; i++)
            if (positionByInt(i) != null) {
                smell[i] = positionByInt(i).getPheromone();
                sum += smell[i];
            }
        
        //正規化
        for (int i = 0; i < 8; i++) smell[i] /= sum;
        
        //消臭フェロモン拡散がONの場合
        if (Core.isProposalDiffusiton) {
            double min = -50;
            for (int i = 0; i < 8; i++) {
                if(positionByInt(i) !=null) {
                    if (positionByInt(i).getRawPheromone() < min) {
                        deodorantDiffusion(position, 3, 0.50);
                        break;
                    }
                }
            }
        }
        return smell;

    }

    //消臭フェロモンを周囲に発見したらrange分だけ,ratioにもとづいて削減を行う
    private void deodorantDiffusion(Position pos, int range, double ratio) {
        //見つけた中心点
        pos.reducePheromoneByRatio(1 - ratio);
        //上方部分
        upper(pos, range, ratio);
        //下方部分
        downer(pos, range, ratio);
        //右方部分
        righter(pos, range, ratio);
        //左方部分
        lefter(pos, range, ratio);

        //斜め4方向
        reduceUp_right(pos, range, ratio);
        reduceDown_right(pos, range, ratio);
        reduceDown_left(pos, range, ratio);
        reduceUp_left(pos, range, ratio);
    }

    //配列(ここでは8方向のフェロモン量が格納された配列)を累積化する
    /*
    private double[] accumulatedSum(double[] array) {
        double sum = 0;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
            array[i] = sum;
        }
        array[array.length - 1] = 1;
        return array;
    }
    */

    //累積配列に基づいて確立的に方向を返す
    private int chooseRandomIndex(double[] array) {
        double r = Math.random();
        int i = 0;
        while(array[i]<r) i++;
        return i;
    }
    
    //x軸で反転させる
    private Position coordinatePosition(Position before){
        Position after = (Position)before.clone();//情報が書き換えられないように複製する
        after.y = (Core.world_y - after.y);
        return after;
    }
    
    //2点間の直座標系に対する角度(radian)を求める
    protected double getRadian(Position compare){
        double radian;
        Position current;
        current = position;
        current = coordinatePosition(current);
        radian = Math.atan2(compare.y - current.y, compare.x - current.x);
        return radian;
    }

    //角度を元にクラスタリングを行う
    private int clusterbyInt(double radian) {
        if((-(Math.PI/8) <= radian && 0 > radian)||(0 <= radian && (Math.PI/8) > radian)){
            return 6;
        }else if((Math.PI/8) <= radian && (Math.PI*3/8) > radian){
            return 7;
        }else if((Math.PI*3/8) <= radian && (Math.PI*5/8) > radian){
            return 0;
        }else if((Math.PI*5/8) <= radian && (Math.PI*7/8) > radian){
            return 1;
        }else if(((Math.PI*7/8) <= radian && (Math.PI) >= radian)||(-(Math.PI) < radian && -(Math.PI*7/8) >= radian)){
            return 2;
        }else if(-(Math.PI*7/8) <= radian && -(Math.PI*5/8) > radian){
            return 3;
        }else if(-(Math.PI*5/8) <= radian && -(Math.PI*3/8) > radian){
            return 4;
        }else if(-(Math.PI*3/8) <= radian && -(Math.PI/8) > radian){
            return 5;
        }else{
            //到達エージェントがゼロの時はランダム
            int random;
            random = (int)(Math.random()*8);
            System.out.println("random walk: " + radian);
            return random;
        }
    }
    
    //2点間の距離を求める
    private double getDistance(Position dst){
        Position src;
        double distance;
        src = position;
        src = coordinatePosition(src);
        distance = Math.sqrt(Math.pow((src.x - dst.x), 2) + Math.pow((src.y - dst.y), 2));
        return distance;
    }

    //ゴールしたエージェントとの距離,ベクトルをもとにした八方向の累積した配列を返す
    private double[] smellReachedAnts(Ant[] another){
        double[] smellReach = new double[8];
        int sum = 0;

        for(int i=0;i<8;i++)smellReach[i]=0;
        
        for (Ant anAnother : another) {
            if (anAnother.isgoal==1) {
                double radian;
                int clusterNumber;
                double distance;
                radian = getRadian(coordinatePosition(anAnother.position));
                clusterNumber = clusterbyInt(radian);
                distance = getDistance(coordinatePosition(anAnother.position));
                smellReach[clusterNumber] += 1/Math.sqrt(distance);
            }
        }
        for(int i=0;i<8;i++) sum += smellReach[i];
        //到達エージェントがゼロの時にゼロ除算を行いNANになるのを防ぐ
        if(sum!=0){
            for (int i = 0; i < 8; i++) smellReach[i] /= sum;//正規化
        }else{
            for (int i = 0; i < 8; i++) smellReach[i] = 0;
        }
            return smellReach;
    }

    //フェロモン量、ゴールエージェントの位置にもとづいた値を割合化した配列を返す
    private double[] accumulatedMultiIndex(double pheromone[], double reached[], double alpha) {
        double[] sum = new double[8];

        double sum_p = 0;
        double sum_g = 0;
        for (int i = 0; i < pheromone.length; i++) {
            sum_p += pheromone[i];
            pheromone[i] = sum_p;
        }

        for (int i = 0; i < reached.length; i++) {
            sum_g += reached[i];
            reached[i] = sum_g;
        }

        for(int i = 0; i < sum.length; i++){
            sum[i] = alpha*pheromone[i] + (1-alpha)*reached[i];
        }
        sum[sum.length - 1] = 1;
        return sum;
    }

    //移動関数
    public boolean move() {
    	//ゴールまたは死亡していた場合動かない
        if (this.isgoal==1 || this.isdead == 1) return false;
        if (path_hash.put(position.x + ", " + position.y, position) == null) path.add(position);
        //直前Nつの状態の保持
        System.arraycopy(prePosition, 1, prePosition, 0, prePosition.length - 1);
        prePosition[prePosition.length - 1] = this.position;
        
        //エージェントの死亡判定
        for(Position aDangerArea : dangerArea){
            if(isSamePoint(position, aDangerArea)){
                this.deth_tsunami = 1;
                this.isdead = 1;
            }
        }
        for(Position aFireArea : fireArea){
            if(isSamePoint(position, aFireArea)){
                this.deth_fire = 1;
                this.isdead = 1;
            }
        }
        
        //1:ゴール時に塗布 2:移動ごとに塗布
        switch (Core.applymethod) {
            case 1:
                if (path_hash.put(position.x + ", " + position.y, position) == null) path.add(position);
                //移動
                if (!walkStep(chooseRandomIndex(accumulatedMultiIndex(smellPheromone(), smellReachedAnts(this.anotherAgent), alpha)))) return false;
                
                //ゴール処理
                for (Position aPath : path) aPath.addPheromone(addPheromone);
                this.isgoal = 1;
                path.clear();
                path_hash.clear();
                return true;
                
            case 2:
                if (!walkStep(chooseRandomIndex(accumulatedMultiIndex(smellPheromone(), smellReachedAnts(this.anotherAgent), 0.8)))) return false;
                //if (walkStep(chooseRandomIndex(accumulatedSum(smellPheromone())))) return true;
                position.addPheromone(addPheromone);
                return false;
            default:
                return false;
        }
    }
    
    private void reduceUp_right(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        while (i < range) {
            if (isStopDiffusion(currentPosition.up_right())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.up_right().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.up_right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.up_right().up_right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_right();
            i++;
        }
    }

    private void reduceDown_right(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        while (i < range) {
            if (isStopDiffusion(currentPosition.down_right())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.down_right().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.down_right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.down_right().down_right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_right();
            i++;
        }
    }

    private void reduceDown_left(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        while (i < range) {
            if (isStopDiffusion(currentPosition.down_left())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.down_left().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.down_left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.down_left().down_left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_left();
            i++;
        }
    }

    private void reduceUp_left(Position pos, int range, double ratio) {
        Position currentPosition = pos;

        int i = 1;
        while (i < range) {
            if (isStopDiffusion(currentPosition.up_left())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.up_left().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.up_left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.up_left().up_left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_left();
            i++;
        }
    }

    private void upper(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        boolean isreduce = false;
        //直上
        while (i < range) {
            if (isStopDiffusion(currentPosition.up())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.up().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.up().up().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.up().up().up().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.up().up().up_right()))
                            pos.up().up().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.up().up().up_left()))
                            pos.up().up().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        isreduce = true;
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up();
            i++;
        }

        //左上
        currentPosition = pos;
        int j = 1;
        while (j < range) {
            if (isStopDiffusion(currentPosition.up())) {
                break;
            } else {
                switch (j) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.up().up_left()))
                            pos.up().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.up().up_left().up_left()))
                            pos.up().up_left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.up().up_left().up())))
                            pos.up().up_left().up().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_left();
            j++;
        }

        int k = 1;
        while (k < range) {
            if (isStopDiffusion(currentPosition.up())) {
                break;
            } else {
                switch (k) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.up().up_right()))
                            pos.up().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.up().up_right().up_right()))
                            pos.up().up_right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.up().up_right().up())))
                            pos.up().up_right().up().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_right();
            k++;
        }
    }

    private void downer(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        boolean isreduce = false;
        //直上
        while (i < range) {
            if (isStopDiffusion(currentPosition.down())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.down().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.down().down().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.down().down().down().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.down().down().down_right()))
                            pos.down().down().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.down().down().down_left()))
                            pos.down().down().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        isreduce = true;
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down();
            i++;
        }

        //左上
        currentPosition = pos;
        int j = 1;
        while (j < range) {
            if (isStopDiffusion(currentPosition.down())) {
                break;
            } else {
                switch (j) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.down().down_left()))
                            pos.down().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.down().down_left().down_left()))
                            pos.down().down_left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.down().down_left().down())))
                            pos.down().down_left().down().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_left();
            j++;
        }

        int k = 1;
        while (k < range) {
            if (isStopDiffusion(currentPosition.down())) {
                break;
            } else {
                switch (k) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.down().down_right()))
                            pos.down().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.down().down_right().down_right()))
                            pos.down().down_right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.down().down_right().down())))
                            pos.down().down_right().down().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_right();
            k++;
        }
    }

    private void righter(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        boolean isreduce = false;
        //直上
        while (i < range) {
            if (isStopDiffusion(currentPosition.right())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.right().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.right().right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.right().right().right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.right().right().up_right()))
                            pos.right().right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.right().right().down_right()))
                            pos.right().right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        isreduce = true;
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.right();
            i++;
        }

        //左上
        currentPosition = pos;
        int j = 1;
        while (j < range) {
            if (isStopDiffusion(currentPosition.right())) {
                break;
            } else {
                switch (j) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.right().down_right()))
                            pos.right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.right().down_right().down_right()))
                            pos.right().down_right().down_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.right().down_right().right())))
                            pos.right().down_right().right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_right();
            j++;
        }

        int k = 1;
        while (k < range) {
            if (isStopDiffusion(currentPosition.right())) {
                break;
            } else {
                switch (k) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.right().up_right()))
                            pos.right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.right().up_right().up_right()))
                            pos.right().up_right().up_right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.right().up_right().right())))
                            pos.right().up_right().right().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_right();
            k++;
        }
    }

    private void lefter(Position pos, int range, double ratio) {
        Position currentPosition = pos;
        int i = 1;
        boolean isreduce = false;
        //直上
        while (i < range) {
            if (isStopDiffusion(currentPosition.left())) {
                break;
            } else {
                switch (i) {
                    case 1:
                        pos.left().reducePheromoneByRatio(1 - (ratio * ratio));
                        break;
                    case 2:
                        pos.left().left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        pos.left().left().left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.left().left().up_left()))
                            pos.left().left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if (!isStopDiffusion(pos.left().left().down_left()))
                            pos.left().left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        isreduce = true;
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.left();
            i++;
        }

        //左上
        currentPosition = pos;
        int j = 1;
        while (j < range) {
            if (isStopDiffusion(currentPosition.left())) {
                break;
            } else {
                switch (j) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.left().down_left()))
                            pos.left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.left().down_left().down_left()))
                            pos.left().down_left().down_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.left().down_left().left())))
                            pos.left().down_left().left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.down_left();
            j++;
        }

        int k = 1;
        while (k < range) {
            if (isStopDiffusion(currentPosition.left())) {
                break;
            } else {
                switch (k) {
                    case 1:
                        break;
                    case 2:
                        if (!isStopDiffusion(pos.left().up_left()))
                            pos.left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio));
                        break;
                    case 3:
                        if (!isStopDiffusion(pos.left().up_left().up_left()))
                            pos.left().up_left().up_left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        if ((!isreduce) && (!isStopDiffusion(pos.left().up_left().left())))
                            pos.left().up_left().left().reducePheromoneByRatio(1 - (ratio * ratio * ratio * ratio));
                        break;
                    default:
                        break;
                }
            }
            currentPosition = currentPosition.up_left();
            k++;
        }
    }
}


