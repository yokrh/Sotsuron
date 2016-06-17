import java.text.DecimalFormat;
import java.util.Random;


public class Othello_main_CUI {

	public static void main(String[] args) {

		// ~8/2(Sat)	- HL2層ver(別ファイル)
		// 8/3(Sun)		- HL3層ver
		// 9/14(Sun)	割と大幅リファクタリング
		//				Harmony後攻も追加
		//				NNのbias部にバグ発見、修正（以前の実験データ死…
		// 9/15(Mon)	Fの評価も変更（f = √f先行 * f後攻）（先行のみ強くなるのを防ぐため
		// 9/25(Thr)	- VS_other_harmony ver
		//				main_CUIを大幅変更（対戦形式変更のため）
		// 10/5(Sun)	COM_HSにMinimax(dpt==2)追加。COM_HSリファクタリング
		//				NNinputをpiecediffの枝以外は完全にアノ論文のものに。
		// 10/14(Tue)	フィルタバグをfix...orz
		//				informationにturnを追加
		// 10/25(Sat)   - ForkJoin ver に移行


		/* TODO */
		/*

		・優先度たかい
		Minimaxの確認。完成。
		αβ化

		・優先度ふつう
		0) NNinput の なかみを色々試してみる（お互いの着手可能手とか、ターン数とか、フィルタ変えたりとか）
		1) HSパラメータ調整
		2) randが規則的で実はやばい？（8/28）
		-> System.currentTimeMillis() + Runtime.getRuntime().freeMemory() : シード
		-> SecureRandom() : 分布がいい
		-> メルセンヌツイスターが最強らしい。
		-> 対戦の精度上がれば、対戦数減らせる気がする。ただ、そっちの研究は今回はしない。いまは放置なう

		 */

		/* MEMO */ /*

		// Minimax (αβ)
		 depth = 2 と 3 で全然オーダーが違い、スムーズといえるのは2まで
		・Apply to Harmony
		もう１度、試してみる
		・Apply to COM_HS
		もう１度、試してみる
		[ 注意すること ]
		置き場所がないときや、残りターンが探索の深さより浅いときにバグがあるのかなあ

		// Activate関数
		・arctan
		試してみようか
		・ReLU
		使ったらなぜかerror。（ReLU : 最近人気のActivate関数。論理演算の時は使ってみたら精度落ちた）


		// NNinput
		・hidden_layer_size(IL_len)
		Board Itself Information + Additional Information
		Additional Information 候補 : （my/oppの） 着手可能手, 開放度, 固定石
		[ 歴史 ]
		∑k^2 (k=1~6) : block 8*8 ～ 3*3 （微妙改善足りなかったかなあ）
		∑k^2 (k=8, 6, 1) : block 8*8, 3*3, 1*1 （悪くない)
		--- vsHarmony化 ---
		∑k^2 (k=8, 3, 2, 1) : block 8*8 ～ 3*3 + 追加情報（まあ、よくなった）

		// VS COM_AI（教師）
		オセロの本質をHarmonyに覚えさせるにはいい教師を作るのが大事だと思い、
		いい教師をつくることに専念していたが、間違っていた。
		この方法だと、教師ピンポイント対策Harmonyしかできない！
		原因としては、COM_AIとHS(NN)は打ち方が一意なために、試合内容が同じ物ばかりになり、
		NNは教師のある程度のパターンに勝てればよい状態となっているから。(AIの戦術の幅は狭い！)
		オセロの本質を覚える学習に向いていない。
		ちなみに、先行のみ強い局所解ができることがある。
		[ Strong AI ]
		・COM_AI(Easy)
		 : Piece Differential. まあ初心者。弱い
		 Harmonyは勝率7割超えた。
		・COM_AI(Normal)
		 : Board Evaluation. 対人だとそこそこ強い。後半弱い傾向がある
		 from オセロサイト（http://uguisu.skr.jp/othello/5-1.html）
		 Harmonyは勝率6割超えた。

		・評価関数
		勝ち:10, 引き分け:1, 負け0

		・F精度
		そもそもゲームの強さは相対評価。多少のムラが存在する。（偽優良、過小評価）

		// HS
		・FW, par
		普通の設定
		・Allowed range
		abs(1.0)
		・MemoryUpdate
		普通の設定
		・ACCIDENTALING
		なし。F精度がよくないため微調整は効果的ではない

		// 乱数
		※今回の研究からは対象外にします
		メルセンヌツイスターが最強らしい。よゆうがあれば利用したい
		乱数の偏り = 置く場所の偏り、なので、注意しないと行けない。

		 */

		/* オーダー */
		/*

		未計算。とっても大きい

		*/


		// Universal
		DecimalFormat df = new DecimalFormat("###.##");

		// Board setting
		final int empty = 0;				//
		final int black = 1;				//
		final int white = -1;				//
		final int BoardSize = 8;

		// NN Setting
		final int information_size = 2;
			// piece_differencial + turn
		final int input_layer_size   = BoardSize * BoardSize + information_size;
		final int hidden_layer_size  = 91 + information_size;
			// 1 + 4 + 9 + 16 + 25 + 36 + information_size
		final int hidden_layer_size2 = 40;
		final int hidden_layer_size3 = 10;
		final int output_layer_size  = 1;

		// HM1TF Setting
		final Boolean HM1TF[][] = new Boolean[input_layer_size][hidden_layer_size];
		// フィルタの役割になるように
		for(int i=0;i<input_layer_size;i++) for(int j=0;j<hidden_layer_size;j++) HM1TF[i][j] = false;
		int it = 0;
		for(int k=BoardSize;k>=1;k--){
			if(k != 8 && k != 7 && k != 6 && k != 5 && k != 4 && k != 3) continue;
			int t = BoardSize + 1 - k;
			for(int y=0;y<t;y++){
				for(int x=0;x<t;x++){
					Boolean Tmp[][] = new Boolean[BoardSize][BoardSize];
					for(int i=0;i<BoardSize;i++){
						for(int j=0;j<BoardSize;j++){
							if((x <= j && j < x + k) && (y <= i && i < y + k)) Tmp[i][j] = true;
							else Tmp[i][j] = false;
						}
					}
					//for(int i=0;i<8;i++){ for(int j=0;j<8;j++) System.out.print(" " + (Tmp[i][j] ? 1 : 0)); System.out.println();} System.out.println();
					for(int i=0;i<BoardSize;i++) for(int j=0;j<BoardSize;j++) HM1TF[i * BoardSize + j][it] = Tmp[i][j];
					it++;
				}
			}
		}
		for(int i=0;i<information_size;i++){
			HM1TF[input_layer_size - 1 - i][hidden_layer_size - 1 - i] = true;
		}
		//System.out.println(input_layer_size + " " + hidden_layer_size + " " + information_size);
		//for(int i=0;i<HM1TF.length;i++){ for(int j=0;j<HM1TF[0].length;j++) System.out.print(HM1TF[i][j] ? 1 : 0); System.out.println();}

		// HS Setting
		int HarmonyMemorySize = 50;
		int MaximumImprovisation = 3;
		HarmonySearch HS = new HarmonySearch(HarmonyMemorySize, MaximumImprovisation, HM1TF,
				input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size);

		// Game Setting
		int MaxOppNum = 10;
		int MaxGametimes = 1;

		// Computer Setting
		//none

		/* 実行時間計測 */ long start = System.nanoTime();

		// HS Initialize
		HS.RandomTuning();
		FileInputOutput_HS fi = new FileInputOutput_HS();
		fi.Input(HS);
		HS.displayF();
		HS.displayBest();

		// Improve HS by Playing Game
		for(int imp=0; imp<HS.mi; imp++){
			System.out.println("[[ imp / mi ]] imp : " + imp);

			// 新世代生成
			HS.HarmonyImprovization();

			// Fitness 計算
			// 初期化
			for(int k=0;k<HS.hms;k++) HS.F[k] = 0.0;
			for(int k=0;k<HS.hms;k++) HS.NewF[k] = 0.0;
			// Playing Game
			// 新世代 vs 旧世代
			System.out.print("[ New vs Old ] ");
			for(int k=0;k<HS.hms;k++){
				System.out.print(".");
				for(int k2_=0;k2_<MaxOppNum;k2_++){
					int k2 = (k + k2_) % HS.hms;
					for(int t=0;t<MaxGametimes;t++){
						// Player1 Setting (New Harmony, k)
						NeuralNetwork NN1 = new NeuralNetwork(input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size,
								HS.NewH1[k], HS.NewH2[k], HS.NewH3[k], HS.NewH4[k]);
						Computer_HS COM_HS1 = new Computer_HS(NN1);
						Computer PL1 = new Computer(COM_HS1);

						// Player2 Setting (Old Harmony, k2)
						NeuralNetwork NN2 = new NeuralNetwork(input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size,
								HS.HM1[k2], HS.HM2[k2], HS.HM3[k2], HS.HM4[k2]);
						Computer_HS COM_HS2 = new Computer_HS(NN2);
						Computer PL2 = new Computer(COM_HS2);

						// Play Game
						PL1.setColor(new Random(System.currentTimeMillis()).nextBoolean() ? black : white);
						PL2.setColor(PL1.oppcolor);
						Game GameMaster = new Game(PL1, PL2);
						Pii result = GameMaster.Play();

						// Evaluate F
						HS.NewF[k] += HS.CalcF(result);
						HS.F[k2] += HS.CalcF(new Pii(result.second, result.first));
					}
				}
			}
			System.out.println();
			// 正規化
			for(int k=0;k<HS.hms;k++) HS.F[k] /= 1.0 * MaxOppNum * MaxGametimes;
			for(int k=0;k<HS.hms;k++) HS.NewF[k] /= 1.0 * MaxOppNum * MaxGametimes;
			// 観察
			if(imp % 100 == -1){
				System.out.print("New F :  "); for(int k=0;k<HS.hms;k++) System.out.print(df.format(HS.NewF[k]) + ", "); System.out.println();
				System.out.print("Old F :  "); for(int k=0;k<HS.hms;k++) System.out.print(df.format(HS.F[k]) + ", "); System.out.println();
			}

			// HM更新
			HS.MemoryUpdate();

			// vs Observer
			if(imp % 100 == 0){
				System.out.println();
				HS.displayBest3FAndGame();
			}
		}

		if(HS.mi != 0){
			// Modify Fitness in HM
			// Game Setting
			MaxOppNum = HS.hms - 1;
			MaxGametimes = 10;
			// 初期化
			for(int k=0;k<HS.hms;k++) HS.F[k] = 0.0;
			for(int k=0;k<HS.hms;k++) HS.NewF[k] = 0.0;
			// 対戦
			System.out.print("[ Modify Fitness ] ");
			for(int k=0;k<HS.hms;k++){
				System.out.print(".");
				for(int k2=k+1;k2<HS.hms;k2++){
					for(int t=0;t<MaxGametimes;t++){
						// Player1 Setting (New Harmony, k)
						NeuralNetwork NN1 = new NeuralNetwork(input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size,
								HS.HM1[k], HS.HM2[k], HS.HM3[k], HS.HM4[k]);
						Computer_HS COM_HS1 = new Computer_HS(NN1);
						Computer PL1 = new Computer(COM_HS1);

						// Player2 Setting (Old Harmony, k2)
						NeuralNetwork NN2 = new NeuralNetwork(input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size,
								HS.HM1[k2], HS.HM2[k2], HS.HM3[k2], HS.HM4[k2]);
						Computer_HS COM_HS2 = new Computer_HS(NN2);
						Computer PL2 = new Computer(COM_HS2);

						// Play Game
						PL1.setColor(t % 2 == 0 ? black : white);
						PL2.setColor(PL1.oppcolor);
						Game GameMaster = new Game(PL1, PL2);
						Pii result = GameMaster.Play();

						// Evaluate F
						HS.F[k]  += HS.CalcF(result);
						HS.F[k2] += HS.CalcF(new Pii(result.second, result.first));
					}
				}
			}
			System.out.println();
			// 正規化
			for(int k=0;k<HS.hms;k++) HS.F[k] /= 1.0 * HS.hms * MaxGametimes;
			HS.displayF();
		}

		// vs Observer
		System.out.println(); System.out.println();
		HS.displayFAndGame();

		/* 実行時間計測 */
		double runtime = (System.nanoTime() - start);
		System.out.println();
		System.out.println("Runtime:" + df.format(runtime / Math.pow(10, 9) / 60) + "min (" + df.format(runtime / Math.pow(10, 9)) + " 秒)");
		System.out.println();

		// Output File which has HM data
		FileInputOutput_HS fo = new FileInputOutput_HS();
		fo.Output(HS);

		// Extra
		Boolean extra_flg = false;
		if(extra_flg){
			Extra(HarmonyMemorySize, MaximumImprovisation, HM1TF,
					input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size);
		}

		return;
	}


	// Extra
	private static void Extra(int HarmonyMemorySize, int MaximumImprovisation, Boolean[][] HM1TF,
			int input_layer_size, int hidden_layer_size, int hidden_layer_size2, int hidden_layer_size3, int output_layer_size){
		// combine 2 HS
		String file_name1 = "Othello_AI_weight1-4_learned_0804-0006_Normal_IL101.txt";
		String file_name2 = "Othello_AI_weight1-4_learned_0804-0201_Normal_IL101_何か1回でそこそこ強いのできた.txt";
		HarmonySearch HS1 = new HarmonySearch(HarmonyMemorySize, MaximumImprovisation, HM1TF,
				input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size);
		HarmonySearch HS2 = new HarmonySearch(HarmonyMemorySize, MaximumImprovisation, HM1TF,
				input_layer_size, hidden_layer_size, hidden_layer_size2, hidden_layer_size3, output_layer_size);
		FileInputOutput_HS fio = new FileInputOutput_HS();
		if(HS1.hms >= HS2.hms){
			fio.InputwithFilepath(HS1, file_name1);
			fio.InputwithFilepath(HS2, file_name2);
		}else{
			fio.InputwithFilepath(HS1, file_name2);
			fio.InputwithFilepath(HS2, file_name1);
		}
		fio.OutputWithCombiningHS(HS1, HS2);
		return;

	}

}
