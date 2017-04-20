package wumpusworld;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    boolean first;
    // holds X and Y coordinates and for directions at each state
    int n[][][]; //holds the repetiton count of state-action pair
    int q[][][];// holds q-learning info of each state-action pair
    int size;
    int prevX,prevY;//holds previous position info
    int prevDir;// holds previous direction
    
    public static final int WALL = -99999;
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;
        first=true;
        size=w.getSize();
        n=new int[size+1][size+1][5];
        q=new int[size+1][size+1][5];
        for(int i=1;i<=size;i++)
            for(int j=1;j<=size;j++)
                for(int k=0;k<5;k++)
                {
                    n[i][j][k]=0;
                    q[i][j][k]=-1;
                }
        
        // marking map borders as least reward
        q[1][1][2]=WALL;
        q[1][1][3]=WALL;
        q[1][2][3]=WALL;
        q[1][3][3]=WALL;
        q[1][4][0]=WALL;
        q[1][4][3]=WALL;
        q[2][1][2]=WALL;
        q[2][4][0]=WALL;
        q[3][1][2]=WALL;
        q[3][4][0]=WALL;
        q[4][1][1]=WALL;
        q[4][1][2]=WALL;
        q[4][2][1]=WALL;
        q[4][3][1]=WALL;
        q[4][4][0]=WALL;
        q[4][4][1]=WALL;
        
        prevX=1;
        prevY=1;
        prevDir=World.DIR_RIGHT;
    }
   
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {           //run simulation if its the first time this map is used
        if(first)
        {
            simulate(w);
            first=false;           
        }
        else // follow the path with highest calculated reward 
        {
            q_learn(w);
        }
    }    
    
    public void simulate(World w2)  // runs the simulation on a given map for 100 times
    {
        
        for(int count=1; count<100;count++)
        {
            World simw=w2.cloneWorld();
            while(!simw.gameOver())
                q_learn(simw);
            if(simw.gameOver())
                if(simw.hasWumpus(simw.getPlayerX(),simw.getPlayerY()))// keep track of wumpus position
                {
                    q[prevX][prevY][4]=prevDir; 
                   System.out.println("////////////////////////////////////////////////////////"+simw.getPlayerX()+simw.getPlayerY());         
                }
        }
        
    }
    public void q_learn(World w)
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
               
        //Test the environment
        int reward=0;
        if (w.hasBreeze(cX, cY))
        {
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            System.out.println("I am in a Pit:"+cX+" "+cY);
        }
        if (w.hasWumpus(cX, cY))
        {
            reward+=-1000;
            System.out.println("Wumpus ate me!!");
            q[prevX][prevY][4]=prevDir;
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up:"+cX+" "+cY);
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            reward+=10000;
            w.doAction(World.A_GRAB);            
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            reward+=-5000;
            w.doAction(World.A_CLIMB);
        }
        //Shoot wumpus and move
        if(q[cX][cY][4]>-1)
            {
                turn(q[cX][cY][4], w);
                if(w.hasArrow())   
                    w.doAction(World.A_SHOOT);
                w.doAction(World.A_MOVE);
                return;
            }
        // actual q-learning steps
        int max=-999999;
        for(int k=0;k<4;k++)// keeps track of highest rewarding state action pair to be used in the formula.
        {
            if(q[cX][cY][k]>max)
            {
                max=q[cX][cY][k];
            }
        }
        double alpha=(double)1/(1+n[prevX][prevY][prevDir]);
        double gamma=0.5;
        q[prevX][prevY][prevDir]+=(double)(alpha*(reward+gamma*max-q[prevX][prevY][prevDir]));
        //decide next move
        
        //make simulation move and update the count of that move
        int move = decideMove(cX,cY); 
        prevX=cX;
        prevY=cY;
        prevDir=move;      
        turn(move,w);
        n[cX][cY][move]++;
        w.doAction(World.A_MOVE);             
    }
    
    
    // turn towards the decided directon
    public void turn(int move, World w)
    {
        if (move==World.DIR_UP)
        {
            if (w.getDirection() == World.DIR_RIGHT)
            {
                w.doAction(World.A_TURN_LEFT);                
            }
            if (w.getDirection() == World.DIR_LEFT)
            {
                w.doAction(World.A_TURN_RIGHT);
            }
            if (w.getDirection() == World.DIR_UP)
            {
            }
            if (w.getDirection() == World.DIR_DOWN)
            {
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_TURN_LEFT);
            }
        }
        
        if (move==World.DIR_RIGHT)
        {
            if (w.getDirection() == World.DIR_DOWN)
            {
                w.doAction(World.A_TURN_LEFT);                
            }
            if (w.getDirection() == World.DIR_UP)
            {
                w.doAction(World.A_TURN_RIGHT);
            }
            if (w.getDirection() == World.DIR_RIGHT)
            {
            }
            if (w.getDirection() == World.DIR_LEFT)
            {
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_TURN_LEFT);
            }
        }
                
        if (move==World.DIR_DOWN)
        {
            if (w.getDirection() == World.DIR_LEFT)
            {
                w.doAction(World.A_TURN_LEFT);                
            }
            if (w.getDirection() == World.DIR_RIGHT)
            {
                w.doAction(World.A_TURN_RIGHT);
            }
            if (w.getDirection() == World.DIR_DOWN)
            {
            }
            if (w.getDirection() == World.DIR_UP)
            {
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_TURN_LEFT);
            }
        }
                        
        if (move==World.DIR_LEFT)
        {
            if (w.getDirection() == World.DIR_UP)
            {
                w.doAction(World.A_TURN_LEFT);               
            }
            if (w.getDirection() == World.DIR_DOWN)
            {
                w.doAction(World.A_TURN_RIGHT);
            }
            if (w.getDirection() == World.DIR_LEFT)
            {
            }
            if (w.getDirection() == World.DIR_RIGHT)
            {
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_TURN_LEFT);
            }
        }
    }

            // decides a move based on exploration vs exploitation
    public int decideMove(int x, int y)
    {
        int leastn=999;
        int leastn_dir=0;
        for(int k=0;k<4;k++)
        {
            if((n[x][y][k]<leastn)&&(q[x][y][k]!=-99999))
            {
                leastn=n[x][y][k];
                leastn_dir=k;
            }
        }
        if(leastn<7)
            return leastn_dir;
        else
        {
            int maxrew=-9999999;
            int maxrew_dir=0;
            for(int k=0;k<4;k++)
            {
                if(q[x][y][k]>maxrew)
                {
                    maxrew=q[x][y][k];
                    maxrew_dir=k;
                }
            }
            return maxrew_dir;
        }
    }
}

