import java.text.DecimalFormat;
import java.util.Random;


public class Game {
	// オセロゲーム

	int empty;
	int black;
	int white;
	int Boardsize;
	Board BD;

	Computer COM1;
	Computer COM2;
	int margin_turn = 20; 						//パスが起きるとターン数は60を超える
	int Maxturn = 8 * 8 - 4 + margin_turn;		//最大ターン数

	Random rand = new Random(System.currentTimeMillis());
	DecimalFormat df = new DecimalFormat("###.##");


	Game(Computer COM1, Computer COM2){
		this.COM1 = COM1;
		this.COM2 = COM2;

		Parameters Params = new Parameters();
		this.empty = Params.empty;
		this.black = Params.black;
		this.white = Params.white;
		this.Boardsize = Params.BoardSize;
	}

	// 一試合行い、結果(COM1の石数, COM2の石数)を返す
	public Pii Play(){
		// 試合を行う
		Board BD = new Board();
		BD.setBoardGameStart();
		for(int t=0;t<this.Maxturn;t++){
			//System.out.println("turn t : " + t);
			if(!BD.hasNextMov(this.black) && !BD.hasNextMov(this.white)) break;
			int bw = t % 2 == 0 ? this.black : this.white;
			// COM1 turn
			if(bw == COM1.color){
				Pii nx = COM1.getNextMove_xy(BD.Board, t);
				//System.out.println(nx.first + " " + nx.second);
				if(nx.first == -1 && nx.second == -1) continue;
				else{
					BD.updateBoard(COM1.color, nx);
					//System.out.println("COM1 : " + nx.second + " " + nx.first);
				}
			}
			// COM2 turn
			if(bw == COM2.color){
				Pii nx = COM2.getNextMove_xy(BD.Board, t);
				//System.out.println(nx.first + " " + nx.second);
				if(nx.first == -1 && nx.second == -1) continue;
				else{
					BD.updateBoard(COM2.color, nx);
					//System.out.println("COM2 : " + nx.second + " " + nx.first);
				}
			}
			//System.out.println("turn: " + t);
			//BD.displayBoard();
		}
		//BD.displayBoard();
		//BD.displayBoardResult();

		// 石数の結果を求める
		Pii result = BD.getBlackDiscnumAndWhiteDiscnum();
		int bcnt = result.first;
		int wcnt = result.second;
		if(this.COM1.color == this.white) result = new Pii(wcnt, bcnt);
		return result;
	}
}
