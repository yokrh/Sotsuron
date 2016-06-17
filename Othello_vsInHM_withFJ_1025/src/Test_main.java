import java.text.DecimalFormat;
import java.util.Random;


public class Test_main {

	// Universal
	static DecimalFormat df = new DecimalFormat("###.##");

	// Paramaters
	static Parameters Params = new Parameters();

	// HS
	static HarmonySearch HS = new HarmonySearch(
			Params.HarmonyMemorySize, Params.MaximumImprovisation, Params.getHM1TF(),
			Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
			Params.hidden_layer_size3, Params.output_layer_size);

	public static void main(String[] args) {

		Params.displayParams();

		/* 実行時間計測 */
		long start = System.nanoTime();

		// HS Initialize
		HS.RandomTuning();
		if(Params.fi_flg){
			FileInputOutput_HS fi = new FileInputOutput_HS();
			fi.Input(HS);
		}
		HS.displayF();
		HS.displayBest();

		System.out.println("[ [ [  No Improvisation ! ] ] ]"); System.out.println();
		System.out.println("[ [ [ It's TEST ! ] ] ] "); System.out.println();


		// 対戦 inHM
		System.out.println("[ [ vs inHM ] ]");
		{
			int MaxOppNum = -1, MaxGametimes = -1;
			int l = 0, r = 1;
			for(int k=l;k<r;k++){
				// Player1 Setting (New Harmony, k)
				NeuralNetwork NN1 = new NeuralNetwork(
						Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
						Params.hidden_layer_size3, Params.output_layer_size,
						HS.HM1[k], HS.HM2[k], HS.HM3[k], HS.HM4[k]);
				int depth = 2;
				Computer PL1 = new Computer(new Computer_HS(NN1, depth));

				for(int k2_=0;k2_<MaxOppNum;k2_++){
					int k2 = (k + k2_) % HS.hms;
					if(k == k2) continue;
					for(int t=0;t<MaxGametimes;t++){
						// Player2 Setting (Old Harmony, k2)
						NeuralNetwork NN2 = new NeuralNetwork(
								Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
								Params.hidden_layer_size3, Params.output_layer_size,
								HS.HM1[k2], HS.HM2[k2], HS.HM3[k2], HS.HM4[k2]);
						Computer PL2 = new Computer(new Computer_HS(NN2, depth));

						// Play Game
						System.out.println(k + " vs " + k2);
						PL1.setColor(new Random(System.currentTimeMillis()).nextBoolean() ? Params.black : Params.white);
						PL2.setColor(PL1.oppcolor);
						Game GameMaster = new Game(PL1, PL2);
						GameMaster.Play();
					}
				}
			}
		}

		// 対戦 vsObserver
		System.out.println("[ [ vs Observer ] ]");
		{
			final int n = 5;
			int MaxGametimes = 50;
			System.out.println("[ The Best" + n + " Harmony vs Observer ] " + MaxGametimes + " times");


			int best[] = new int[n];
			for(int k=0;k<best.length;k++){
				best[k] = 0;
				for(int i=1;i<HS.hms;i++){
					Boolean flg = false;
					for(int j=0;j<k && !flg;j++) if(i == best[j]) flg = true;
					if(flg) continue;
					if(HS.F[i] > HS.F[best[k]]) best[k] = i;
				}
			}
			Boolean[] isEliteHarmony = new Boolean[HS.hms];
			for(int k=0;k<isEliteHarmony.length;k++) isEliteHarmony[k] = false;
			for(int i=0;i<best.length;i++) isEliteHarmony[best[i]] = true;

			int l = 0, r = HS.hms;
			for(int k=l;k<r;k++){
				if(isEliteHarmony[k] == false) continue;

				// Player1 Setting (New Harmony, k)
				NeuralNetwork NN1 = new NeuralNetwork(
						Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
						Params.hidden_layer_size3, Params.output_layer_size,
						HS.HM1[k], HS.HM2[k], HS.HM3[k], HS.HM4[k]);
				/* 実験ポイント */
				int depth = 1;
				Computer PL1 = new Computer(new Computer_HS(NN1, depth));

				// Play Game
				// Player2 Setting (Observer)
				Computer PL2;

				// Easy
				PL2 = new Computer(new Computer_Easy());
				System.out.println("vs Easy");

				System.out.println("P : Black");
				PL1.setColor(Params.black);
				PL2.setColor(PL1.oppcolor);
				VsComputerNtimes(PL1, PL2, k, MaxGametimes, PL1.color == Params.black);

				System.out.println("P : White");
				PL1.setColor(Params.white);
				PL2.setColor(PL1.oppcolor);
				VsComputerNtimes(PL1, PL2, k, MaxGametimes, PL1.color == Params.black);

				// Normal
				PL2 = new Computer(new Computer_Normal());
				System.out.println("vs Normal");

				System.out.println("P : Black");
				PL1.setColor(Params.black);
				PL2.setColor(PL1.oppcolor);
				VsComputerNtimes(PL1, PL2, k, MaxGametimes, PL1.color == Params.black);

				System.out.println("P : White");
				PL1.setColor(Params.white);
				PL2.setColor(PL1.oppcolor);
				VsComputerNtimes(PL1, PL2, k, MaxGametimes, PL1.color == Params.black);

				System.out.println();
			}
		}

		/* 実行時間計測 */
		double runtime = (System.nanoTime() - start);
		System.out.println("Runtime:" + df.format(runtime / Math.pow(10, 9) / 60) + "min (" + df.format(runtime / Math.pow(10, 9)) + " 秒)");
		System.out.println();


		return;
	}

	// vs Computer（N試合）
	private static void VsComputerNtimes(Computer PL, Computer COM, int k, int MaxGametimes, Boolean isPlayerFirst){
		int win = 0, lose = 0, draw = 0;
		for(int i=0;i<MaxGametimes;i++){
			Pii res = VsComputer(PL, COM, isPlayerFirst);
			if(res.first == res.second) draw++;
			else if (res.first > res.second) win++;
			else lose++;
		}
		System.out.println("No." + k + " (F:" + df.format(HS.F[k]) + ")   "  + "win : " + win + " , draw : " + draw + " , lose : " + lose);
		return;
	}

	// vs Computer（1試合）
	private static Pii VsComputer(Computer PL, Computer COM, Boolean isPlayerFirst){
		// Game Preprocess
		int player_color   = isPlayerFirst ? Params.black : Params.white;
		int computer_color = isPlayerFirst ? Params.white : Params.black;
		PL.setColor(player_color);
		COM.setColor(computer_color);

		// Play Game
		Game GameMaster = new Game(PL, COM);
		Pii result = GameMaster.Play();
		return result;
	}
}