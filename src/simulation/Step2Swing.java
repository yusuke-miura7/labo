package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
/**
 * Created by hiroq7 on 15/01/23.
 */
import javax.swing.JPanel;

//1step毎に描写する
public class Step2Swing extends JPanel implements Runnable {
	
	//描画を行うかどうかを決定するフラグ
	public static boolean is_pheromone;
    public static boolean is_grid;
    public static boolean is_agents;
    public static boolean is_object;
    public static boolean is_safe;
    public static boolean is_fire;
    public static boolean is_route;
    
    //描画要素
    static Space space;
    
    static ArrayList<Node> nodeArray;
    static ArrayList<Position> wallArray;
    static ArrayList<Position> buildingArray;
    static ArrayList<Position> safeArray;
    static ArrayList<Position> tsunamiAreaArray;
    static ArrayList<Position> riverArray;
    static Position[] fireArray;
    static ArrayList<Agent> agentArray;
    static Astar2[] agentArray2;
    
    //サイズ
    public static float window_width = 600;
    public static float window_height = 600;
    private static int world_x;
    private static int world_y;
    public static int offset_x = 1;
    public static int offset_y = 1;
    
    //描画色
    final Color safeColor = new Color(50, 205, 50);
    final Color gridColor = new Color(105, 105, 105);
    final Color blockObjectColor = Color.BLACK;
    final Color dangerAreaColor = Color.BLUE;
    final Color fireAreaColor = Color.RED;
    
    //3次元表示
    public Iso3D iso3D = new Iso3D();
    public int step = 50;
    public int originX = 500;
    public int originY = 400;
    public int xs = Core.xSquareSize-20;
    public int ys = Core.ySquareSize-20;
    public float ySkew = 3.0f;
    
    //コンポーネント
    JButton mode0Button,mode1Button,mode2Button;
    JButton leftButton,rightButton,upButton,downButton;
    JButton yIncButton,yDecButton;
    JButton yInc2DButton,yDec2DButton;
    JButton zoomInButton,zoomOutButton;
    
    //コストラクタ
    public Step2Swing() {
        Thread refresh = new Thread(this);
        refresh.start();
        
        mode0Button = new JButton("Mode Left");
		mode0Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				iso3D.mode=0;
			}
		});
		
		mode1Button = new JButton("Mode Center");
		mode1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				iso3D.mode=1;
			}
		});
		
		mode2Button = new JButton("Mode Right");
		mode2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				iso3D.mode=2;
			}
		});
		
		leftButton = new JButton("Left");
		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				originX-=step;
			}
		});
		
		rightButton = new JButton("Right");
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				originX+=step;
			}
		});
		
		upButton = new JButton("Up");
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				originY-=step;
			}
		});
		
		downButton = new JButton("Down");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				originY+=step;
			}
		});
		
		yIncButton = new JButton("Y+");
		yIncButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ySkew+=0.1f;
				if(ySkew==0) ySkew = 0.1f;
			}
		});
		
		yDecButton = new JButton("Y-");
		yDecButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ySkew-=0.1f;
				if(ySkew==0) ySkew = -0.1f;
			}
		});
		
		yInc2DButton = new JButton("2D Y+");
		yInc2DButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ySkew+=.1f;
				if(ySkew==0.0f) ySkew = 0.1f;
			}
		});
		
		yDec2DButton = new JButton("2D Y-");
		yDec2DButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ySkew-=.1f;
				if(ySkew==0.0f) ySkew = -0.1f;
			}
		});
		
		zoomInButton = new JButton("Zoom Out");
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				xs-=1;
				ys-=1;
			}
		});
		
		zoomOutButton = new JButton("Zoom In");
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				xs+=1;
				ys+=1;
			}
		});
		
        is_grid = true;
        is_agents = true;
        is_object = true;
        is_safe = true;
        is_fire = true;
        //is_pheromone = true;
        is_pheromone = false;
        is_route = true;
    }

    public static void update(int x, int y,Space space,ArrayList<Position> wall,ArrayList<Position> building, ArrayList<Position> safe,ArrayList<Agent> agentArray,ArrayList<Node> nodeArray) {
    	Step2Swing.world_x = x;
        Step2Swing.world_y = y;
        Step2Swing.space = space;
        Step2Swing.wallArray = wall;
        Step2Swing.buildingArray = building;
        Step2Swing.safeArray = safe;
        //Step2Swing.dangerAreaArray = danger;
        Step2Swing.nodeArray = nodeArray;
        Step2Swing.agentArray = agentArray;
    }
    
    public static void update_danger(ArrayList<Position> tsunami,ArrayList<Position> river) {
    	Step2Swing.tsunamiAreaArray = tsunami;
    	Step2Swing.riverArray = river;
    }
    
    //3次元表示でボタンUIを追加する
    public void changeUI() {
    	if(Core.mapDimension==3) {
    		this.removeAll();
    		this.add(mode0Button);
            this.add(mode1Button);
            this.add(mode2Button);
            this.add(leftButton);
            this.add(rightButton);
            this.add(upButton);
            this.add(downButton);
            this.add(yIncButton);
            this.add(yDecButton);
            this.add(yInc2DButton);
            this.add(yDec2DButton);
            this.add(zoomInButton);
            this.add(zoomOutButton);
            Core.pauseFlag=true;
            Core.changeFlag=false;
    	}
    	
    	if(Core.mapDimension==2) {
    		this.removeAll();
    		Core.pauseFlag=false;
    		Core.changeFlag=false;
    	}
    }
    
    //描画関数
    public void paint(Graphics g) {
        if(Core.changeFlag) {
    		changeUI();
    	}
        
        super.paint(g);
        
        //2次元表示
        if(Core.mapDimension==2) {
        	draw_outrect(g);
            if (is_pheromone) draw_pheromone(g);
            if(is_object) {
            	draw_wall(g);
            	draw_building(g);
            	draw_node(g);
            }
            draw_tsunami(g);
            draw_river(g);
            //if(is_safe) draw_safe(g);
            if (is_fire) draw_fire(g);
            if (is_agents) {
            	draw_agents(g);
            }
            if (is_grid) draw_grids(g);
            if(is_route) draw_tracingagents(g);
            
        }
        
        //3次元表示
        if(Core.mapDimension==3) {
        	g.setColor(Color.green);
        	//g.fillRect(0, 0, 1000, 1000);
        	Iso3D.Point2D point1 = null;
        	Iso3D.Point2D point2 = null;
        	for(int y=0;y<world_y;y++) {
        		for(int x =0;x<world_x;x++) {
        			try {
        				if(x<world_x-1) {
        					point1=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y, x).z * ySkew),y*ys));
        					point2=iso3D.transform3D(iso3D.new Point3D((x+1)*xs,(int)(space.position(y, x+1).z * ySkew),y*ys));
        					
        					g.drawLine(point1.x+originX,point1.y+originY,point2.x+originX,point2.y+originY);
        				}
        				if(y<world_y-1) {
        					point1=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y, x).z* ySkew),y*ys));
        					point2=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y+1, x).z* ySkew),(y+1)*ys));
        					
        					g.drawLine(point1.x+originX,point1.y+originY,point2.x+originX,point2.y+originY);
        				}
        			}catch(Exception e) {
        				e.printStackTrace();
        			}
        		}
        	}
        	/*
        	try {
				draw_agents3d(g);
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}*/
        	
        }
    }
    
  //エージェントの描画
    public void draw_agents3d(Graphics g) throws Exception {
    	Iso3D.Point2D point1 = null;
    	Iso3D.Point2D point2 = null;
    	//Iso3D.Point2D point3 = null;
    	Iso3D.Point2D point4 = null;
    	
    	int x,y;
        if (agentArray != null) {
        	for(Agent k:agentArray) {
                Position p = k.returnPos();
                g.setColor(Color.red);
                x=p.x;
                y=p.y;
                point1=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y, x).z * ySkew),y*ys));
                point2=iso3D.transform3D(iso3D.new Point3D((x+1)*xs,(int)(space.position(y, x+1).z * ySkew),y*ys));
                //g.drawLine(point1.x+originX,point1.y+originY,point2.x+originX,point2.y+originY);
                
                //point3=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y, x).z* ySkew),y*ys));
				point4=iso3D.transform3D(iso3D.new Point3D(x*xs,(int)(space.position(y+1, x).z* ySkew),(y+1)*ys));
				//g.drawLine(point3.x+originX,point3.y+originY,point4.x+originX,point4.y+originY);
                
				g.fillPolygon(new int[] {point1.x+originX,point2.x+originX,point4.x+originX},new int[] {point1.y+originY,point2.y+originY,point4.y+originY}, 3);
				//g.fillRect(point1.x+originX,point , , );
				/*g.fillRect(
                        (int) (offset_x + p.x * (window_width / world_x)),
                        (int) (offset_y + p.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y)
                );
                */
            }
        }
    }

    @Override
    public void run() {
        while (!Core.endflag) {
            repaint();
        }
    }
    
    //全体の大枠
    public void draw_outrect(Graphics g) {
        g.setColor(gridColor);
        g.drawLine(offset_x,
                offset_y,
                offset_x,
                (int) (offset_y + window_height)
        );
        g.drawLine(
                (int) (offset_x + window_width),
                offset_y,
                (int) (offset_x + window_width),
                (int) (offset_y + window_height)
        );

        g.drawLine(offset_x,
                offset_y,
                (int) (offset_x + window_width),
                offset_y
        );
        g.drawLine(offset_x,
                (int) (offset_y + window_height),
                (int) (offset_x + window_width),
                (int) (offset_y + window_height)
        );
    }
    
    //フェロモン濃度の描画、色を濃度に反映させるか
    public void draw_pheromone(Graphics g) {
        for (int i = 0; i < world_x; i++) {
            for (int j = 0; j < world_y; j++) {
            	//tmpcolが小さいほど濃いピンク色になる
                double tmpcol = (1 - (space.position(i, j).getPheromone() / space.MMAS_maxPheromone));
                if (tmpcol < 0) {
                    tmpcol = 0;
                }
                g.setColor(new Color(255,
                        (int) (255 * (tmpcol)), 255));
                if (space.position(i, j).getPheromone() == 1.00) {
                    g.setColor(new Color(255, 255, 255));
                }
                g.fillRect(
                        (int) (offset_x + i * (window_width / world_x)),
                        (int) (offset_y + j * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y));
            }
        }
    }
    
    //ノードのデバック用
    public void set_color(Graphics g,int i) {
    	int n = i%10;
    	switch(n) {
    	case 0:
    		g.setColor(new Color(255,75,0));//
    		break;
    	case 1:
    		g.setColor(new Color(255,241,0));//
    		break;
        case 2:
        	g.setColor(new Color(3,175,122));//
        	break;
        case 3:
    		g.setColor(new Color(0,90,255));//
    		break;
        case 4:
    		g.setColor(new Color(77,196,255));
    		break;
        case 5:
    		g.setColor(new Color(255,128,130));
    		break;
        case 6:
    		g.setColor(new Color(246,170,0));
    		break;
        case 7:
    		g.setColor(new Color(153,0,153));
    		break;
        case 8:
    		g.setColor(new Color(128,64,0));
    		break;
        case 9:
    		g.setColor(new Color(132,145,158));
    		break;
    	}
    }
    
    //wallの描画
    public void draw_wall(Graphics g) {
        g.setColor(blockObjectColor);
        if (wallArray != null) {
            for (Position aWallArray : wallArray) {
                g.fillRect(
                        (int) (offset_x + aWallArray.x * (window_width / world_x)),
                        (int) (offset_y + aWallArray.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y));
            }
        }
    }
    
    //nodeの描画
    public void draw_node(Graphics g) {
    	//g.setColor(new Color(0,255,255));
    	//g.setColor(Color.red);
    	if(nodeArray!=null) {
    		for(Node node:nodeArray) {
    			if(node.number==118) {
    				g.setColor(Color.yellow);
    			}
    			else if(node.number==12) {
    				g.setColor(Color.green);
    			}
    			else {
    				g.setColor(Color.red);
    			}
    			//set_color(g,node.number);
    			g.fillRect(
    					(int) (offset_x + node.x * (window_width / world_x)),
    					(int) (offset_y + node.y * (window_height / world_y)),
    					(int) (window_width /world_x),
    					(int) (window_height / world_y));
    		}
    	}
    }
    
    //建物の描画
    public void draw_building(Graphics g) {
    	g.setColor(new Color(50,130,130));
    	if(buildingArray !=null) {
    		for(Position building :buildingArray) {
    			g.fillRect(
    					(int) (offset_x + building.x * (window_width / world_x)),
    					(int) (offset_y + building.y * (window_height / world_y)),
    					(int) (window_width /world_x),
    					(int) (window_height / world_y));
    		}
    	}
    }
    
    //安全地帯の描画
    public void draw_safe(Graphics g) {
        g.setColor(safeColor);
        if (safeArray != null) {
            for (Position aFoodArray : safeArray) {
                g.fillRect(
                        (int) (offset_x + aFoodArray.x * (window_width / world_x)),
                        (int) (offset_y + aFoodArray.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y));
            }
        }
    }
    
    //津波の描画
    public void draw_tsunami(Graphics g) {
        g.setColor(dangerAreaColor);
        if (tsunamiAreaArray != null) {
            for (Position atsunamiAreaArray : tsunamiAreaArray) {
                g.fillRect(
                        (int) (offset_x + atsunamiAreaArray.x * (window_width / world_x)),
                        (int) (offset_y + atsunamiAreaArray.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y));
            }
        }
    }
    
    public void draw_river(Graphics g) {
    	 g.setColor(dangerAreaColor);
         if (riverArray != null) {
             for (Position ariverArray : riverArray) {//
                 g.fillRect(
                         (int) (offset_x + ariverArray.x * (window_width / world_x)),
                         (int) (offset_y + ariverArray.y * (window_height / world_y)),
                         (int) (window_width / world_x),
                         (int) (window_height / world_y));
             }
         }
    }
    
    //fireの描画
    public void draw_fire(Graphics g) {
        g.setColor(fireAreaColor);
        if (fireArray != null) {
            for (Position aFireArray : fireArray) {
                g.fillRect(
                        (int) (offset_x + aFireArray.x * (window_width / world_x)),
                        (int) (offset_y + aFireArray.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y));
            }
        }
    }
    
    //エージェントの描画
    public void draw_agents(Graphics g) {
        if (agentArray != null) {
        	for(Agent k:agentArray) {
        		
                Position p = k.returnPos();
                g.setColor(Color.orange);
                g.fillRect(
                        (int) (offset_x + p.x * (window_width / world_x)),
                        (int) (offset_y + p.y * (window_height / world_y)),
                        (int) (window_width / world_x),
                        (int) (window_height / world_y)
                );
            }
        }
    }

    //枠線(grid)の描画
    public void draw_grids(Graphics g) {
        g.setColor(gridColor);
        for (int x = 0; x <= world_x; x++) {
            g.drawLine(
                    (int) (offset_x + x * (window_width / world_x)),
                    (int) (offset_y),
                    (int) (offset_x + x * (window_width / world_x)),
                    (int) (offset_y + window_height)
            );
        }
        
        for (int y = 0; y <= world_y; y++) {
            g.drawLine((int) (offset_x),
                    (int) (offset_y + y * (window_height / world_y)),
                    (int) (offset_x + window_width),
                    (int) (offset_y + y * (window_height / world_y))
            );
        }
    }
    
    //消臭フェロモンを描画
    public void draw_reduce(Graphics g) {
        g.setColor(Color.orange);
        for (int i = 0; i < world_x; i++) {
            for (int j = 0; j < world_y; j++) {
                double rawPheromone = space.position(i, j).getRawPheromone();
                if (rawPheromone <= -50.00) {
                    g.fillRect(
                            (int) (offset_x + i * (window_width / world_x)),
                            (int) (offset_y + j * (window_height / world_y)),
                            (int) (window_width / world_x),
                            (int) (window_height / world_y));
                }
            }
        }
    }
    
    //火災配列を更新する
    public static void expand(Position[] fire){
        Step2Swing.fireArray = fire;
    }
    
    //エージェントの移動軌跡を描画
    public void draw_tracingagents(Graphics g) {
        g.setColor(Color.red);
        Graphics2D g2 = (Graphics2D)g;
        BasicStroke bs = new BasicStroke(5);
		g2.setStroke(bs);
        if (agentArray != null) {
            for (Agent anAgentArray : agentArray) {
                ArrayList<Position> p = anAgentArray.return_traveld_path();
                for (int i = 0; i < p.size()-1; i++) {
                	g.drawLine(
                            (int) (offset_x+p.get(i).x *(window_width / world_x)),
                            (int) (offset_y+p.get(i).y *(window_width / world_x)),
                            (int) (offset_x+p.get(i+1).x *(window_width / world_x)),
                            (int) (offset_y+p.get(i+1).y *(window_width / world_x))
                    );
                }
            }
        }
    }
}


