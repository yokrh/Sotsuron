import java.util.ArrayList;


public class Computer_HS extends Computer_Base{
	// Computer(Harmony)
	// 次手 : NNによる局面評価の価値が最大

	NeuralNetwork NN;					// NN
	int turn;							// 経過ターン数(未使用)

	int depth = 2;						// αβ探索の深さ
	final double EPS = 1e-8;			// 誤差
	final double MIN_NUM = 0.0;			// 最大値
	final double MAX_NUM = 999.9;		// 最小値

	// αβ探索のdebug用
	//int leaf_cnt;
	//int num[] = {2,5,4,7,4,2,3};
	//int num_it = 0;


	Computer_HS(NeuralNetwork NN){
		this.NN = NN;
	}
	Computer_HS(NeuralNetwork NN, int depth){
		if(depth < 1){
			System.out.println("! depth_input must be >= 1  dpt : " + depth);
			depth = this.depth;
		}
		this.NN = NN;
		this.depth = depth;
	}

	// 次の手を選択する
	// 探索のdepthを一つ分進めているのは、探索の返り値はdoubleで、このメソッドの返り値はPiiのため
	public Pii getNextMove_xy(int[][] BDarray, int turn){
		Pii res = new Pii(-1, -1);
		double res_val = this.MIN_NUM;
		this.turn = turn;
		//this.leaf_cnt = 0;
		//this.num_it = 0;

		ArrayList<Pii> vP = new ArrayList<Pii>();
		vP = this.getValidNextMoves(this.mycolor, BDarray);
		if(vP.isEmpty()) return res;

		for(int i=0;i<vP.size();i++){
			Pii pos = vP.get(i);
			Board NextBD = new Board(BDarray);
			NextBD.updateBoard(this.color, pos);

			double val = this.Alphabeta(this.oppcolor, NextBD.Board, 2 * (this.depth - 1), res_val, this.MAX_NUM);	//αβ
			//double val = this.Minimax(this.oppcolor, NextBD.Board, 2 * (this.depth - 1));	// Minimax(Negamax)
			//double val = this.Minimax(this.oppcolor, NextBD.Board, 0);				// Minimaxなし。原因探し用
			if(res_val < val){
				res_val = val;
				res = pos;
			}
			//System.out.println("-----");
		}
		//for(int _=0;_<2*depth;_++) System.out.print("  "); System.out.println(res_val + "☆");
		//System.out.println(res.first + " " + res.second); try{Thread.sleep(1000);}catch(InterruptedException ex){} System.out.println();
		//System.out.print(" " + this.turn + ":" + this.leaf_cnt + " , ");
		return res;
	}

	// αβ
	// player は相手の利を最小（ == 自分の利を最大 becauseゼロ和）にする手を選ぶ
	private double Alphabeta(int my_color, int[][] BD, int dpt, double alpha, double beta){
		//System.out.println(" " + dpt + " " + alpha + " " + beta);
		double res = (my_color == this.mycolor ? this.MIN_NUM : this.MAX_NUM);
		int opp_color = -1 * my_color;

		if(dpt == 0){
			//this.leaf_cnt++;
			if(my_color != this.oppcolor) System.out.println("dpt setting error!");
			res = this.Evaluation(BD);
			//res = this.num[this.num_it]; this.num_it++;
			//res = 10 + new Random().nextInt(90);
			//for(int i=0;i<dpt;i++) System.out.print("  "); System.out.println("盤" + res + "(" + alpha + "," + beta + ")");
			return res;
		}

		ArrayList<Pii> vP = new ArrayList<Pii>();
		vP = this.getValidNextMoves(my_color, BD);
		if(vP.isEmpty()){
			res = this.Alphabeta(opp_color, BD, dpt - 1, alpha, beta);
			//new Board(BD).displayBoard(); System.out.println("!!! " + my_color + " hasNoMove!   depth:" + dpt);
		}
		for(int i=0;i<vP.size();i++){
			Pii pos = vP.get(i);
			Board NextBD = new Board(BD);
			NextBD.updateBoard(my_color, pos);
			if(my_color == this.mycolor){
				double val = this.Alphabeta(opp_color, NextBD.Board, dpt - 1, res, beta);
				res = Math.max(res, val);
				if(beta + EPS < res) break;
			}else{
				double val = this.Alphabeta(opp_color, NextBD.Board, dpt - 1, alpha, res);
				res = Math.min(res, val);
				if(res < alpha - EPS) break;
			}
		}
		//for(int i=0;i<dpt;i++) System.out.print("  "); System.out.println((my_color==this.mycolor?"自":"敵") + res + "(" + alpha + "," + beta + ")"+vP.size());
		return res;
	}

	// 場所の利の評価
	public double Evaluation(int[][] BDarray){
		double res = 0;
		Board BD = new Board(BDarray);
		for(int i=0;i<4;i++){
			res += this.Evaluation_part(BD.Board);
			if(i != 3) BD.rotateBoardInCounterClockwise();
			//BD.displayBoard();
			//System.out.println(res);
		}
		//System.out.println();
		return res;
	}
	private double Evaluation_part(int BD[][]){
		double res = 0;
		NeuralNetwork NN = this.NN;
		int[] NNinput = new int[NN.IL.length];

		// 純粋な盤情報をinput（ただし、"盤上のBlack(=1)が自分"で）
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) NNinput[i * this.Boardsize + j] = BD[i][j];
		if(this.mycolor != black) for(int i=0;i<NNinput.length;i++) NNinput[i] *= -1;

		// 純粋な盤情報以外の情報をinput
		for(int i=0;i<NNinput.length - this.Boardsize * this.Boardsize;i++){
			if(i == 0) NNinput[i] = this.getPieceNum(BD, this.mycolor);
			if(i == 1) NNinput[i] = this.getPieceNum(BD, this.oppcolor);
			if(i == 2) NNinput[i] = this.getValidNextMoves(this.mycolor, BD).size();
			if(i == 3) NNinput[i] = this.getValidNextMoves(this.oppcolor, BD).size();
		}
		res = NN.Execute(NNinput);
		//new Board(BD).displayBoard(); System.out.println("my_color == " + (my_color == black ? "黒" : "白") + "  res : " + df.format(res));
		return res;
	}

	// NNinput_information_helpers
	// 石数を取得する
	private int getPieceNum(int[][] BD, int color){
		int res = 0;
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) if(BD[i][j] == color) res++;
		return res;
	}
}
