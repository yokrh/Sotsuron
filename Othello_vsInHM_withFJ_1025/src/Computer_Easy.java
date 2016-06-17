import java.util.ArrayList;


public class Computer_Easy extends Computer_Base{
	// Computer(よわい)
	// 次手 : 自分の石数が最大

	Computer_Easy(){}

	// 次の手を選択する
	public Pii getNextMove_xy(int[][] BDarray){
		ArrayList<Pii> vP = new ArrayList<Pii>();
		vP = this.getValidNextMoves(this.mycolor, BDarray);

		if(vP.isEmpty()) return new Pii(-1, -1);

		int i2 = 0, cnt = -(1<<10);
		for(int i=0;i<vP.size();i++){
			Board BD = new Board(BDarray);
			BD.updateBoard(this.mycolor, vP.get(i));
			int cnt2 = this.Evaluation(this.mycolor, BD.Board);

			if(cnt2 > cnt || (cnt2 == cnt && (rand.nextInt(vP.size()) == 0))){
				cnt = cnt2;
				i2 = i;
			}
		}
		return new Pii(vP.get(i2).first, vP.get(i2).second);
	}

	// 盤評価（自分の石の数 - 相手の石の数）
	private int Evaluation(int my_color, int[][] BD){
		int res = 0;
		for(int i=0;i<this.Boardsize;i++) for(int j=0;j<this.Boardsize;j++) if(BD[i][j] == this.mycolor) res++;
		return res;
	}
}
