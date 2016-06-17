import java.util.ArrayList;


public class Computer_Normal extends Computer_Base{
	// Computer(ふつう)
	// 次手 : 石の位置による局面評価の価値が最大

	int[][] BoardValue = {
			{  30, -12,  0, -1, -1,  0, -12,  30 },
			{ -12, -15, -3, -3, -3, -3, -15, -12 },
			{   0,  -3,  0, -1, -1,  0,  -3,   0 },
			{  -1,  -3, -1, -1, -1, -1,  -3,  -1 },
			{  -1,  -3, -1, -1, -1, -1,  -3,  -1 },
			{   0,  -3,  0, -1, -1,  0,  -3,   0 },
			{ -12, -15, -3, -3, -3, -3, -15, -12 },
			{  30, -12,  0, -1, -1,  0, -12,  30 },
	};


	Computer_Normal(){}

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
				//System.out.println(i2 + " <- " + i);
				cnt = cnt2;
				i2 = i;
			}
		}
		//System.out.println("候補数 : " + vP.size() + "  i2 : " + i2);
		return new Pii(vP.get(i2).first, vP.get(i2).second);
	}

	// 盤評価（石の位置による局面評価）
	private int Evaluation(int my_color, int[][] BD){
		int res = 0;
		for(int i=0;i<this.Boardsize;i++){
			for(int j=0;j<this.Boardsize;j++){
				if(BD[i][j] == my_color){
					res += this.BoardValue[i][j];
				}else{
					res -= this.BoardValue[i][j];
				}
			}
		}
		return res;
	}
}
