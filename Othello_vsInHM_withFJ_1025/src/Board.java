import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class Board {
	// 盤

	int Boardsize;
	int empty;
	int black;
	int white;

	int Board[][];			// 盤の石の位置情報
	int b_player;			// 黒のプレイヤー
	int w_player;			// 白のプレイヤー

	Random rand = new Random();
	DecimalFormat df = new DecimalFormat("###.##");


	Board(){
		this.setParams();
		this.Board = new int[this.Boardsize][this.Boardsize];
	}
	Board(int BD[][]){
		this.setParams();
		this.Board = new int[this.Boardsize][this.Boardsize];
		this.setBoard(BD);
	}
	private void setParams(){
		Parameters Params = new Parameters();
		this.empty = Params.empty;
		this.black = Params.black;
		this.white = Params.white;
		this.Boardsize = Params.BoardSize;
		return;
	}

	// 盤をゲーム開始時の石配置状態にする
	public void setBoardGameStart(){
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) Board[i][j] = this.empty;
		Board[3][3] = this.white;
		Board[3][4] = this.black;
		Board[4][3] = this.black;
		Board[4][4] = this.white;
		return;
	}

	// 盤をinputの石配置状態に変更する
	public void setBoard(int[][] input){
		for(int i=0;i<input.length;i++) for(int j=0;j<input[0].length;j++) this.Board[i][j] = input[i][j];
		return;
	}

	// 盤を反時計回りに90度回転する
	public void rotateBoardInCounterClockwise(){
		int[][] tmp = new int[this.Boardsize][this.Boardsize];
		//try { this.displayBoard(); Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) tmp[i][j] = this.Board[i][j];
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) this.Board[-j+7][i] = tmp[i][j];
		//try { this.displayBoard(); Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
		return;
	}

	// 位置(x,y) が盤上か判定する
	private Boolean inBoard(int x, int y){
		return 0 <= x && x <= this.Boardsize - 1 && 0 <= y && y <= this.Boardsize - 1;
	}

	// プレイヤーの次手がルール上正しいか判断する
	private Boolean isCorrectNextMov(int my_color, Pii pos){
		int x = pos.first, y = pos.second;
		if(this.Board[y][x] != empty) return false;

		int opp_color = -1 * my_color;
		int dx[] = {1, 1, 0, -1, -1, -1 , 0,  1};
		int dy[] = {0, 1, 1,  1,  0, -1, -1, -1};
		for(int k=0;k<dx.length;k++){
			int cnt = 0;
			int nx = x + dx[k];
			int ny = y + dy[k];
			while(inBoard(nx, ny) && this.Board[ny][nx] == opp_color){
				nx += dx[k];
				ny += dy[k];
				cnt++;
			}
			if(cnt != 0 && inBoard(nx, ny) && this.Board[ny][nx] == my_color) return true;
		}
		return false;
	}

	// プレイヤーに次手が存在するか判定する
	public Boolean hasNextMov(int my_color){
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++){
			if(this.isCorrectNextMov(my_color, new Pii(i,j))) return true;
		}
		return false;
	}

	// プレイヤーの次手を受け取り、盤の状態を更新する
	public Boolean updateBoard(int my_color, Pii pos){
		if(!this.inBoard(pos.first, pos.second) || !this.isCorrectNextMov(my_color, pos)){
			//System.out.println("Incorrect Next Mov");
			return false;
		}

		int opp_color = -1 * my_color;
		int x = pos.first, y = pos.second;
		int dx[] = {1, 1, 0, -1, -1, -1 , 0,  1};
		int dy[] = {0, 1, 1,  1,  0, -1, -1, -1};
		for(int k=0;k<dx.length;k++){
			ArrayList<Integer> X = new ArrayList<Integer>();
			ArrayList<Integer> Y = new ArrayList<Integer>();
			int nx = x + dx[k];
			int ny = y + dy[k];
			while(inBoard(nx, ny) && this.Board[ny][nx] == opp_color){
				X.add(nx);
				Y.add(ny);
				nx += dx[k];
				ny += dy[k];
			}
			if(X.size() != 0 && inBoard(nx, ny) && this.Board[ny][nx] == my_color){
				this.Board[y][x] = my_color;
				for(int i=0;i<X.size();i++) this.Board[Y.get(i)][X.get(i)] = my_color;
			}
		}
		//System.out.println("Correct Next Mov");
		return true;
	}

	// (黒の石数, 白の石数)を取得する
	public Pii getBlackDiscnumAndWhiteDiscnum(){
		int bcnt = 0, wcnt = 0;
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++){
			if(this.Board[i][j] == this.black) bcnt++;
			if(this.Board[i][j] == this.white) wcnt++;
		}
		return new Pii(bcnt, wcnt);
	}

	// 盤の状態を表示する(黒:1, 白:2, 空: 0)
	public void displayBoard(){
		for(int i=0;i<this.Boardsize;i++){
			for(int j=0;j<this.Boardsize;j++) System.out.print(" " + (this.Board[i][j] == -1 ? 2 : this.Board[i][j]));
			System.out.println();
		}
		System.out.println();
		return;
	}

	// 盤の(黒の石, 白の石, 空)のそれぞれの数を表示する
	public void displayBoardResult(){
		Pii res = this.getBlackDiscnumAndWhiteDiscnum();
		int b = res.first;
		int w = res.second;
		int e = this.Boardsize * this.Boardsize - b - w;
		System.out.println(" result :   " + " Black " + b + "  White " + w + "  Empty " + e);
		return;
	}
}
