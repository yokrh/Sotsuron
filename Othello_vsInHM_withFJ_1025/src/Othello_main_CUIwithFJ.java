import java.text.DecimalFormat;


public class Othello_main_CUIwithFJ {
	// オセロ対戦によるHarmonySearchの学習を行う

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
		// 10/25(Sat)   - ForkJoin ver
		//				ForkJoinのSplitSizeは1だとerrorでる（実装はあってるはず...
		//				αβ探索も実装（できてるはず...
		// 10/30(Sun)   大幅リファクタリング
		//              主にParams, PlayOthelloGame, PlayOthelloGameAction系
		// 11/04(Tue)	inputを 8×8~3×3 から 8×8~2×2,4端,(turn, mypiece, oopppiece) に
		// 11/07(Fri)	input: Block1~8端斜,turn,mypiecenum,opppiecenum,piecediff,turnperiod3
		//				periodてきとーにやったせいか、効果なし。むしろ何か結果がよろしくない
		//				(とにかく間近の端をとるだけの初心者なかんじだった)
		//				(11/04verのOutputでgen900/mi1000のNo65がいいかんじである)
		// 11/11(Tue)	Logの改善(FileOutput修正やParamasOutput追加、vsObserverを%50に)
		//				vsObserver, vsInHMは αβdepth1に
		//				HL2 40->50
		//				input: Block1~8, 端, 斜, turn + my_piece + opp_piece + my_mobility + opp_mobility
		//				mobilityはエキスパートな知識かなあ…とりあえずやってみる
		// 				-> input が多すぎて扱いきれていないと思われる
		//				-> Block 8×8~2×2 で固定の方針にしよう
		// 11/13(Thr)	input: B2~8,端斜, turn, mypiece, opppiece, mymobility, oppmobility でやってみる
		//				hiddenlayer2 を大きくした（40 -> 50）
		//				population 256 にした
		//				結果、まだ、中の下ってかんじ
		// 11/25(Tue)	input: B3~8, B角1~4, B中心1~3, turn, piece_diff, mymobility, oppmobility でやってみる
		//				hiddenlayer2 を大きくした（50 -> 60, なんとなくよくなりそうな気がした）
		//				hiddenlayer3 を大きくした（10 -> 20, なんとなくよくなりそうな気がした）
		//				population 128に戻した（前回そんなに効果あるように思えなかった）
		//				-> 弱かった・・・
		//				原点回帰して、強さの基準を設けよう！
		// 11/27(Thr)	input: B1~8, my/opp-piecenum, my/opp-mobility でやってみる
		//				-> サーバが止まった…imp450で死。logも取り忘れた…
		// 12/02(Tue)	Computer_HSのEvalのresを、BD回転込みの全4パターンをの平均にした（安定化を狙う、反転はなし）
		//				input 変わらず
		//				-> なかなか。 imp400 あたりから vsObserver の勝率は0.9あたりでほぼ横ばい
		//				-> imp750 が一番よかった
		// 12/09(Tue)	vs Observer のdepthを下げた（depth2のデータは取ったため、depth2だと後半の改善度が見れないため）
		//				-> impXXのファイルをinputして、Test_mainで確認すればいい
		//				-> とりあえず保留なう
		// 12/12(Fri)	リファクタリングを行った
		//				( bugfix : パスがあるとMaxturnが60では足りない,
		//				コンストラクタ内でforループが呼ばれる場合に重くなっているところを修正,
		//				特に、NNのWeightはReadOnlyで十分なので、bias追加部分以外はディープコピー(遅い)は必要ないことに気を付ける
		//				コードやコメントをきれいに)
		//				-> 実験データが...
		//				ついでに以下の変更を行い
		//				vsObserver の設定 :  Top 3(<-5) 、alpha-beta-depth 1, 2 (<-2のみ)
		//				にして同様の実験を行う
		//				まあ結果は大丈夫でしょう（グラフ作るのがめんどくさいくらい）
		//
		//				-- 卒論発表の準備もちょくちょく（主に、実験データのサンプル取り） --
		//
		// 12/16(Tue)	Othello_main(GUI)の方をいじった
		//				Java Applet 兼 Java Application にした (main の追加)
		//				WindowLister追加
		//				色々staticにした（CUIの方もそうした方が速くなるのかな）

		/* MEMO */
		/*
		 * HM内の評価を頑張って、妥当なBestを見つけたい！
		 * → HM内で30戦くらい？明確ないい方法が思いつかない。
		 * テストfunctionみたいなの用意するのが一番いい気がするが時間がアレ
		 *
		 * Serverのプロセッサのidle状態の原因探し
		 * → 仕事が少ないからでした。（Eclipse2つ同時起動で100%いった）
		 *
		 * HS.Fの更新の正常性の確認（Rockきちんとされてるか、Conflictないか、のアレ）
		 * → 大丈夫そう
		 */


		// Universal
		DecimalFormat df = new DecimalFormat("###.##");

		// Paramaters
		Parameters Params = new Parameters();
		Params.displayParams();

		/* 実行時間計測 */



		long start = System.nanoTime();


		// HS Initialize
		HarmonySearch HS = new HarmonySearch(
				Params.HarmonyMemorySize, Params.MaximumImprovisation, Params.getHM1TF(),
				Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
				Params.hidden_layer_size3, Params.output_layer_size);
		HS.RandomTuning();
		if(Params.fi_flg){
			FileInputOutput_HS fi = new FileInputOutput_HS();
			fi.Input(HS);
		}
		HS.displayF();
		HS.displayBest();

		// Improve HS by Playing Game
		System.out.println("[ [ [  Improvisation  ] ] ]"); System.out.println();
		for(int imp=0; imp<HS.mi; imp++){
			System.out.print("[[ imp / mi ]] imp : " + imp);
			System.out.print("   ");
			//System.out.println();

			// 新世代生成
			HS.HarmonyImprovization();

			// 対戦 と Fitness 計算
			new PlayOthelloGames(HS).PlayLeagueOldvsNew();

			// HM更新
			HS.MemoryUpdate();

			// 観察（vs Observer）
			if(imp % 50 == 0 && HS.mi != 0){
				new PlayOthelloGames(HS).PlayVsObserver();
				// Output the File which has HM data
				FileInputOutput_HS fo = new FileInputOutput_HS();
				String filename_add = "imp" + imp;
				fo.Output(HS, filename_add);
			}

			System.out.println();
		}
		System.out.println("[ [ [  Improvisation FINISH ] ] ]"); System.out.println();


		/* 実行時間計測 */
		double runtime = (System.nanoTime() - start);
		System.out.println("Runtime:" + df.format(runtime / Math.pow(10, 9) / 60) + "min (" + df.format(runtime / Math.pow(10, 9)) + " 秒)");
		System.out.println();


		// Modify Fitness in HM （Fの再評価:最終HM内の相対評価値となるよう更新）
		System.out.println("[ [ [  Modify Fitness  ] ] ]"); System.out.println();
		new PlayOthelloGames(HS).PlayLeagueInHM();
		System.out.println();
		// Disp
		HS.displayF();
		System.out.println("[ [ [  Modify Fitness FINISH ] ] ]"); System.out.println();


		/* 実行時間計測 */
		runtime = (System.nanoTime() - start);
		System.out.println("Runtime:" + df.format(runtime / Math.pow(10, 9) / 60) + "min (" + df.format(runtime / Math.pow(10, 9)) + " 秒)");
		System.out.println();


		// vs Observer
		new PlayOthelloGames(HS).PlayVsObserver();
		System.out.println();

		// Output the File which has HM data
		FileInputOutput_HS fo = new FileInputOutput_HS();
		fo.Output(HS);

		return;
	}

}
