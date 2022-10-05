package game;


import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.util.Map;
import java.awt.event.WindowAdapter;

public class GUI extends JFrame implements PropertyChangeListener {

    public GameState gameState;
    public String localPlayer;
    public JLabel scoreBoardLabel;
    public JLabel[][] mazeMapGrids;



    public void updateScoreBoardLabel(){
        StringBuilder sb = new StringBuilder("<html> ScoreBoard <br>");
        for(Map.Entry<String,Integer> playerAndScore : this.gameState.scoreBoard.entrySet()){
            sb.append("Player : " + playerAndScore.getKey()).
                    append(" Score : ").
                    append(playerAndScore.getValue()).
                    append("<br>");
        }
        sb.append("</html>");
        scoreBoardLabel.setText(sb.toString());
    }

    public void updateMazeMapGrids() {
        int rows = this.gameState.mazeMap.length;
        int cols = this.gameState.mazeMap.length;
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                Color backgroundColor = Color.white;
                StringBuilder cell = new StringBuilder();
                boolean hasTreasure = false;
                if(gameState.mazeMap[i][j] == '*') {
                    backgroundColor = Color.yellow;
                    hasTreasure = true;
                }
                for (Map.Entry<String, int[]> playerAndPosition : gameState.allPlayerPositions.entrySet()) {
                    if(playerAndPosition.getValue()[0] == i && playerAndPosition.getValue()[1] == j) {
                        cell.append(playerAndPosition.getKey());
                        if(playerAndPosition.getKey().equals(localPlayer)) {
                            backgroundColor = hasTreasure ? Color.red : Color.green;
                        }
                        // can break because we cannot have two players in the same cell
                        break;
                    }
                }
                mazeMapGrids[i][j].setText(cell.toString());
                mazeMapGrids[i][j].setBackground(backgroundColor);
            }
        }
    }

    public GUI (GameState gameState, String localPlayer) {
        setVisible(true);
        int rows = gameState.mazeMap.length;
        int cols = gameState.mazeMap.length;
        this.localPlayer = localPlayer;
        this.gameState = gameState;

        //init scoreboard
        Panel sideInfo = new Panel(new FlowLayout());
        scoreBoardLabel = new JLabel();
        updateScoreBoardLabel();
        scoreBoardLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        scoreBoardLabel.setSize(300, 300);
        sideInfo.add(scoreBoardLabel);

        //init map grids
        Panel map = new Panel(new GridLayout(rows, cols));
        mazeMapGrids = new JLabel[rows][cols];
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mazeMapGrids[i][j] = new JLabel();
                mazeMapGrids[i][j].setOpaque(true);
                mazeMapGrids[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                map.add(mazeMapGrids[i][j]);
            }
        }
        updateMazeMapGrids();
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        add(sideInfo, BorderLayout.WEST);
        setTitle(localPlayer);
        setSize(400, 400);
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Terminating ...");
                System.exit(0);
            }
        });
    }

    public void propertyChange(PropertyChangeEvent event) {
        gameState = (GameState) event.getNewValue();
        updateScoreBoardLabel();
        updateMazeMapGrids();
    }
}
