
public final class Parameters {
	// パラメータ

	// Board setting
	final int BoardSize = 8;			// マス数 in 1辺
	final int empty = 0;				// 空のマス
	final int black = 1;				// 黒のマス
	final int white = -1;				// 白のマス

	// NN Setting
	final int information_size = 4;													// 盤の石の位置以外の情報の数
																					// : my_piece + opp_piece + my_mobility + opp_mobility
	final int input_layer_size   = BoardSize * BoardSize + information_size;		// IL の長さ
	final int hidden_layer_size  = 204 + (information_size);						// HL1の長さ
																					// : Block1~8(204 = 64+49+36+25+16+9+4+1) + information_size
	final int hidden_layer_size2 = 60;												// HL2の長さ
	final int hidden_layer_size3 = 20;												// HL3の長さ
	final int output_layer_size  = 1;												// OLの長さ

	// HM1TF Setting
	private final Boolean HM1TF[][] = new Boolean[input_layer_size][hidden_layer_size];		// ILとHL1の間のweightのフィルタ
	private Boolean initialized_HM1TF = false;												// HM1TFが初期化されているかのフラグ

	// HS Setting
	final int HarmonyMemorySize = 128;			// HarmonyMemorySize
	final int MaximumImprovisation = 1000;		// MaximumImprovisation
	final Boolean fi_flg = true;				// file_input_flag

	// Game Setting
	final int MaxOppNum = (int)Math.sqrt((double)HarmonyMemorySize);	// あるHarmonyの、対戦相手数(学習時)
	final int MaxGametimes = 1;											// あるHarmonyの、ある対戦相手との対戦数(学習時)

	// ModifyF After All Setting
	final int ModifyF_MaxOppNum = HarmonyMemorySize - 1;						// あるHarmonyの、対戦相手数(F修正時)
	final int ModifyF_MaxGametimes = 10;										// あるHarmonyの、ある対戦相手との対戦数(F修正時)


	Parameters(){}

	// HM1TFフィルタの初期化
	public Boolean[][] getHM1TF(){
		if(this.initialized_HM1TF) return this.HM1TF;
 
		int idx = 0;		// フィルタ行列の現在の列を示す役割の補助index
		// falseに初期化
		for(int i=0;i<input_layer_size;i++) for(int j=0;j<hidden_layer_size;j++) this.HM1TF[i][j] = false;
		// 盤ブロック情報フィルタ
		for(int k=BoardSize;k>=1;k--){
			if(k != 8 && k != 7 && k != 6 && k != 5 && k != 4 && k != 3 && k !=  2 && k != 1) continue;
			int t = BoardSize + 1 - k;
			for(int y=0;y<t;y++){
				for(int x=0;x<t;x++){
					Boolean Tmp[][] = new Boolean[BoardSize][BoardSize];
					for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++) Tmp[i][j] = false;
					for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++){
						if((x <= j && j < x + k) && (y <= i && i < y + k)) Tmp[i][j] = true;
					}
					//for(int i=0;i<8;i++){ for(int j=0;j<8;j++) System.out.print(" " + (Tmp[i][j] ? 1 : 0)); System.out.println();} System.out.println();
					for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++) this.HM1TF[i * BoardSize + j][idx] = Tmp[i][j];
					idx++;
				}
			}
		}
		// その他情報フィルタ
		for(int i=0;i<information_size;i++){
			this.HM1TF[input_layer_size - 1 - i][hidden_layer_size - 1 - i] = true;
		}
		//System.out.println(input_layer_size + " " + hidden_layer_size + " " + information_size);
		//for(int i=0;i<HM1TF.length;i++){ for(int j=0;j<HM1TF[0].length;j++) { System.out.print(HM1TF[i][j] ? 1 : 0); System.out.print(",");} System.out.println();}
		this.initialized_HM1TF = true;
		return this.HM1TF;
	}

	// パラメータの値を表示する(Log用)
	public void displayParams(){
		System.out.println("Information_size     : " + this.information_size);
		System.out.println("input_layer_size     : " + this.input_layer_size);
		System.out.println("hidden_layer_size    : " + this.hidden_layer_size);
		System.out.println("hidden_layer_size2   : " + this.hidden_layer_size2);
		System.out.println("hidden_layer_size3   : " + this.hidden_layer_size3);
		System.out.println("HarmonyMemorySize    : " + this.HarmonyMemorySize);
		System.out.println("MaximumImprovisation : " + this.MaximumImprovisation);
		System.out.println("MaxOppNum            : " + this.MaxOppNum);
		System.out.println("MaxGametimes         : " + this.MaxGametimes);
		System.out.println("ModifyF_MaxOppNum    : " + this.ModifyF_MaxOppNum);
		System.out.println("ModifyF_MaxGametimes : " + this.ModifyF_MaxGametimes);
		System.out.println();
	}
}
