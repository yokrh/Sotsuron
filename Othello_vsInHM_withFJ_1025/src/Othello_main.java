import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Othello_main extends Applet implements ActionListener,MouseListener,WindowListener,Runnable{
	// オセロ対戦 のアプレット

	/* TODO */
	/*
	  先攻後攻選択ボタン
	  コンソールへの出力をアプレット上に出す（配布用のため）
	 */

	// Universal
	static Random rand = new Random(System.currentTimeMillis());
	static DecimalFormat df = new DecimalFormat("###.###");

	static Parameters Params = new Parameters();

	// Board setting
	static final int empty = Params.empty;				//
	static final int black = Params.black;				//
	static final int white = Params.white;				//
	static final int BoardSize = Params.BoardSize;

	// HS
	// NN
	static final int information_size = Params.information_size;
	static final int input_layer_size   = Params.input_layer_size;
	static final int hidden_layer_size  = Params.hidden_layer_size;
	static final int hidden_layer_size2 = Params.hidden_layer_size2;
	static final int hidden_layer_size3 = Params.hidden_layer_size3;
	static final int output_layer_size  = Params.output_layer_size;
	static NeuralNetwork NN;
	// HM1TF
	static final Boolean HM1TF[][] = Params.getHM1TF();
	// HSparams
	static final int HarmonyMemorySize = Params.HarmonyMemorySize;
	static final int MaximumImprovisation = 0;
	static final HarmonySearch HS = new HarmonySearch(HarmonyMemorySize, MaximumImprovisation, HM1TF,
			input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size);

	// COM
	static Computer COM;

	// GAME
	static Board BD = new Board();
	static int player_color;
	static int computer_color;
	static int turn_color;
	static int turn;
	static Boolean start_flg = false;
	static Boolean computer_won_prev_game = true;
	static int hs_idx = -1;

	// Applet
	static Frame OthelloFrame = new Frame("Othello");
	static Othello_main OthelloApplet = new Othello_main();
	// Settings
	static final int sqSize = 50;
	static final int cirSize = 30;
	static final int empW = 100;
	static final int empH = 150;
	static final int scrW = 2 * empW + BoardSize * sqSize;
	static final int scrH = 2 * empH + BoardSize * sqSize;
	static Boolean show_validmoves_flg = true;
	static Pii prev_com_move = new Pii(-99, -99);
	static String infoStr = "";
	// GUI Components
	static Panel mainPnl;
	static Button RestartBtn;
	static Button ShowVmBtn;

	// Thread
	static Thread th;


	/* main */
	// Java Application 化
	public static void main(String[] args) {
		OthelloFrame.add(OthelloApplet);
		OthelloFrame.setSize(scrW, scrH);
		OthelloApplet.init();
		OthelloFrame.setVisible(true);
		OthelloFrame.addWindowListener(OthelloApplet);
		OthelloApplet.start();
		return;
	}


	// WindowLister関係
	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		OthelloFrame.dispose();
	}
	public void windowClosed(WindowEvent e) {
		OthelloApplet.stop();
		OthelloApplet.destroy();
        System.exit(0);
	}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	// MouseListener関係
	private Boolean inRange_mouseSquare(Point point, int x, int y){
		if((x + 0.1) * sqSize < point.x && point.x < (x + 0.9) * sqSize
				&& (y + 0.1) * sqSize < point.y && point.y < (y + 0.9) * sqSize) return true;
		return false;
	}
	private Pii getPlayerInputSquare(Point point){
		point.x -= empW;
		point.y -= empH;
		Pii p = new Pii(-1, -1);
		for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++){
			int x = j, y = i;
			if(this.inRange_mouseSquare(point, x, y)){
				p = new Pii(x, y);
				break;
			}
		}
		return p;
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseClicked(MouseEvent e){
		if(turn_color != player_color) return;

		Point point = e.getPoint();
		Pii p = this.getPlayerInputSquare(point);
		infoStr = "プレイヤーがクリックした位置 :  " + p.first + ", " + p.second;
		System.out.println("PL   " + p.first + " " + p.second);

		Boolean updated;
		updated = BD.updateBoard(player_color, p);
		if(updated == false){
			infoStr += " は不正です！";
			System.out.println("Player's next move is wrong!");
		} else if (BD.hasNextMov(computer_color)){
			turn_color = computer_color;
		}

		repaint();
		if(!BD.hasNextMov(computer_color) && !BD.hasNextMov(player_color)) start_flg = false;
		return;
	}

	// ActionListener関係
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == RestartBtn){
			System.out.println("ReStart");
			start_flg= false;
			remove(mainPnl);
			init();
			start_flg = true;
		}
		if(e.getSource() == ShowVmBtn){
			if(show_validmoves_flg){
				infoStr = "置ける位置表示 を 「OFF」 にしました";
				System.out.println("Show OFF");
			} else {
				infoStr = "置ける位置表示 を 「ON」 にしました";
				System.out.println("Show ON");
			}
			show_validmoves_flg ^= true;
			repaint();
		}
		return;
	}

	// 初期化
	public void init(){
		/*  About HS  */
		// HS Initialize
		FileInputOutput_HS fi = new FileInputOutput_HS();
		//fi.Input(HS);
		fi.Input2_jar(HS);
		HS.displayBest();
		//fi.Output(HS, "[ Input()の場所はココだよ！ ]");

		/*  About NN */
		// NN Initialize
		int best = 0;
		for(int i=0;i<HS.hms;i++) if(HS.F[i] > HS.F[best]) best = i;
		if(hs_idx == -1) hs_idx = best;
		System.out.println("computer_won_prev_game : " + computer_won_prev_game);
		if(computer_won_prev_game == false){
			hs_idx = rand.nextInt(HS.hms);
			for(int i=0;i<1000 && HS.F[best]-HS.F[hs_idx]>0.3;i++) hs_idx = rand.nextInt(HS.hms);
		}

		System.out.println("NeuralNetwork <- No." + hs_idx + "(" + df.format(HS.F[hs_idx]) +")" + "(best?:" + best + "("+ df.format(HS.F[best]) +")"+")");
		NN = new NeuralNetwork(input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size,
				HS.HM1[hs_idx], HS.HM2[hs_idx], HS.HM3[hs_idx], HS.HM4[hs_idx]);


		/*  About Computer AI (COM_HS) */
		//Computer_Normal COM_AI_N = new Computer_Normal();
		Computer_HS COM_HS = new Computer_HS(NN);
		COM = new Computer(COM_HS);


		/*  About GAME  */
		// Prepare Game Start
		BD.setBoardGameStart();
		turn_color = black;
		turn = 0;
		prev_com_move = new Pii(-99, -99);
		start_flg = true;
		// Game Preprocess (Deside Colors)
		player_color = (rand.nextBoolean() ? black : white);
		computer_color = -1 * player_color;
		COM.setColor(computer_color);
		infoStr = (player_color == black ? "プレイヤーの先攻です" : "コンピュータの先攻です");
		System.out.println(player_color == black ? "[ player first ]" : "[ computer first ]");


		/*  About GUI  */
		// GUI Components
		this.setSize(scrW, scrH);
		setBackground(Color.CYAN.brighter());
		addMouseListener(this);

		mainPnl = new Panel();

		RestartBtn = new Button(" Restart ");
		ShowVmBtn = new Button(" ON / OFF ");
		RestartBtn.addActionListener(this);
		ShowVmBtn.addActionListener(this);

		Panel[] pnl = new Panel[6];
		for(int i=0;i<pnl.length;i++){
			pnl[i] = new Panel();
			pnl[i].setLayout(new GridLayout(0, 1));
		}
		for(int i=0;i<pnl.length;i++) pnl[i].add(new Label(""));
		pnl[0].add(new Label(""));
		pnl[1].add(new Label(" Restart : ", Label.LEFT));
		pnl[1].add(new Label(" Show move point : ", Label.LEFT));
		pnl[2].add(RestartBtn);
		pnl[2].add(ShowVmBtn);
		pnl[3].add(new Label(" Player : ", Label.RIGHT));
		pnl[3].add(new Label(" Com (" + hs_idx +") : ", Label.RIGHT));
		pnl[4].add(new Label(" " + (player_color == black ? "黒" : "白") + " ", Label.LEFT));
		pnl[4].add(new Label(" " + (computer_color == black ? "黒" : "白") + " ", Label.LEFT));
		pnl[5].add(new Label(""));
		for(int i=0;i<pnl.length;i++) mainPnl.add(pnl[i]);

		this.add(mainPnl);

		// repaint
		repaint();

		// Thread Start
		th = new Thread(this);
		th.start();
	}

	// 処理
	public void run(){
		while(start_flg){
			if(!BD.hasNextMov(computer_color) && !BD.hasNextMov(player_color)) start_flg = false;
			if(turn_color == player_color){
				if(!BD.hasNextMov(player_color)){
					infoStr = "またコンピュータの番！（プレイヤーの石を置ける位置がない）";
					System.out.println("Player has no move ! Computer_AI turn again !");
					turn_color = computer_color;
					turn++;
					continue;
				}
				// COM_AI_X を player とする場合はココ (not User)
				Boolean AutoP_flg = false;
				if(AutoP_flg) this.AutoBattle();
				continue;
			}
			if(turn_color == computer_color){
				if(!BD.hasNextMov(computer_color)){
					infoStr = "またプレイヤーの番！（コンピュータの石を置ける位置がない）";
					System.out.println("Computer has no move ! Player turn again!");
					turn_color = player_color;
					turn++;
					continue;
				}
				// 考えているふり
				try{Thread.sleep(300);}catch(InterruptedException ex){}

				Pii nx = COM.getNextMove_xy(BD.Board, turn);
				infoStr = "";
				infoStr = "コンピュータが置いた位置 :  " + nx.first + ", " + nx.second;
				System.out.println("COM_HS   " + nx.first + " " + nx.second);
				BD.updateBoard(computer_color, nx);

				prev_com_move = nx;
				turn_color = player_color;
				turn++;
			}
			repaint();
		}
		repaint();
		infoStr = " G A M E O V E R ! ";
		System.out.println("GAME OVER");
		System.out.println(player_color == black ? "[ player first ]" : "[ computer first ]");
		BD.displayBoardResult();
		Pii p = BD.getBlackDiscnumAndWhiteDiscnum();
		infoStr += "     黒 " + p.first + " 白 " + p.second + "    結果 : ";
		if(p.first == p.second){
			infoStr += "引き分け";
			System.out.println("Draw");
		} else if(p.first > p.second && player_color == black
				|| p.first < p.second && player_color == white){
			infoStr += "プレイヤーの勝ち";
			System.out.println("Player Win!");
			computer_won_prev_game = false;
		}else{
			infoStr += "コンピュータの勝ち";
			System.out.println("Computer Win!");
			computer_won_prev_game = true;
		}
		return;
	}

	// 描画
	public void paint(Graphics g){
		// 基本盤レイアウト
		for(int i=0;i<BoardSize;i++){
			for(int j=0;j<BoardSize;j++){
				if((i + j ) % 2 == 0) g.setColor(Color.GREEN.darker());
				else g.setColor(Color.GREEN);
				g.fillRect(empW  + i * sqSize, empH + j * sqSize, sqSize, sqSize);
			}
		}
		g.setColor(Color.BLUE);
		g.drawRect(empW, empH, BoardSize * sqSize, BoardSize * sqSize);

		// ディスク
		for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++){
			Pii p = new Pii(i, j);
			int p_color = BD.Board[j][i];
			if(p_color == empty) continue;
			if(p_color == black) g.setColor(Color.BLACK);
			if(p_color == white) g.setColor(Color.WHITE);
			g.fillOval(empW + 10 + p.first * sqSize, empH + 10 + p.second * sqSize, cirSize, cirSize);
		}

		// COMの直前手
		g.setColor(Color.RED);
		g.fillRect(empW + prev_com_move.first * sqSize + 21, empH + prev_com_move.second * sqSize + 21, 10, 10);

		// 置ける場所
		if(show_validmoves_flg && turn_color == player_color){
			ArrayList<Pii> vP = COM.COM_HS.getValidNextMoves(player_color, BD.Board);
			g.setColor(Color.RED);
			for(int i=0;i<vP.size();i++){
				Pii p = vP.get(i);
				g.drawOval(empW  + p.first * sqSize + 10, empH + p.second * sqSize + 10, cirSize, cirSize);
			}
		}

		// Info_String
		g.setColor(Color.BLUE.darker());
		g.setFont(new Font("MS", Font.BOLD, 16));
		g.drawString(infoStr, empW, empH + (BoardSize + 1) * sqSize);

		return;
	}

	// 描画補助
	public void update(Graphics g){
		Image dbImage = null;
		Graphics dbg = null;

		//initialize buffer
		if(dbImage == null){
			dbImage = createImage(this.getSize().width, this.getSize().height);
			dbg = dbImage.getGraphics();
		}

		//clear screen in background
		dbg.setColor(getBackground());
		dbg.fillRect(0, 0, this.getSize().width,  this.getSize().height);

		//draw elements in background
		dbg.setColor(getForeground());
		paint(dbg);

		//draw image on the screen
		g.drawImage(dbImage, 0, 0, this);

	}

	// COM_AI_X を player とする
	private void AutoBattle(){
		// 考えているふり
		try{Thread.sleep(300);}catch(InterruptedException ex){}

		// COM_AI_X 指定はココ
		Computer_Normal COM_AI_N = new Computer_Normal();
		Computer COM = new Computer(COM_AI_N);
		COM.setColor(player_color);

		Pii nx = COM.getNextMove_xy(BD.Board, turn);
		System.out.println("COM_AI_X   " + nx.first + " " + nx.second);
		BD.updateBoard(player_color, nx);

		turn_color = computer_color;
		turn++;

	}

}
