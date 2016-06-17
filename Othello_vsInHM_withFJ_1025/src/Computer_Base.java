import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class Computer_Base {
	// Computerのベースクラス
	// 次手 : ランダム

	int color;					// 自分の色
	int mycolor;				// 自分の色
	int oppcolor;				// 相手の色

	int empty;
	int black;
	int white;
	int Boardsize;

	Random rand = new Random();
	DecimalFormat df = new DecimalFormat("###.##");


	Computer_Base(){}

	// 色を設定する
	public void setColor(int mycolor){
		this.color = mycolor;
		this.mycolor = mycolor;
		this.oppcolor = mycolor * -1 ;

		Parameters Params = new Parameters();
		this.empty = Params.empty;
		this.black = Params.black;
		this.white = Params.white;
		this.Boardsize = Params.BoardSize;
		return;
	}

	// 位置(x,y) が盤上か判定する
	private Boolean inBoard(int x, int y){
		return 0 <= x && x <= this.Boardsize - 1 && 0 <= y && y <= this.Boardsize - 1;
	}

	// 次手のリストを取得する
	public ArrayList<Pii> getValidNextMoves(int my_color, int[][] BD){
		ArrayList<Pii> res = new ArrayList<Pii>();
		int opp_color = -1 * my_color;
		for(int i=0;i<BD.length;i++){
			for(int j=0;j<BD[0].length;j++){
				if(BD[i][j] == this.empty){
					int cnt_sum = 0;
					int x = j, y = i;
					int dx[] = {1, 1, 0, -1, -1, -1 , 0,  1};
					int dy[] = {0, 1, 1,  1,  0, -1, -1, -1};
					for(int k=0;k<dx.length;k++){
						int cnt = 0;
						int nx = x + dx[k];
						int ny = y + dy[k];
						while(inBoard(nx, ny) && BD[ny][nx] == opp_color){
							cnt++;
							nx += dx[k];
							ny += dy[k];
						}
						if(inBoard(nx, ny) && BD[ny][nx] == my_color){
							cnt_sum += cnt;
						}
					}
					if(cnt_sum > 0) res.add(new Pii(x, y));
				}
			}
		}
		return res;
	}

	// 次の手を選択する
	public Pii getNextMove_xy(int[][] BDarray){
		ArrayList<Pii> P = new ArrayList<Pii>();
		P = this.getValidNextMoves(this.mycolor, BDarray);

		if(P.isEmpty()) return new Pii(-1, -1);

		int i2 = rand.nextInt(P.size());
		return new Pii(P.get(i2).first, P.get(i2).second);
	}
}
