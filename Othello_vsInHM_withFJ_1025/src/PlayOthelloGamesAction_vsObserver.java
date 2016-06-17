import static java.util.Arrays.*;

import java.text.DecimalFormat;
import java.util.concurrent.RecursiveAction;


public class PlayOthelloGamesAction_vsObserver extends RecursiveAction {
	int MaxTaskUnitSize = 1 ; 	// tasks are split up to this size
	int l, r;					// [l,r)

	HarmonySearch HS;
	Boolean[] isEliteHarmony;	// F[i]が上位 n位以内 <=> isEliteHarmony[i] == true

	Computer COM;

	Boolean isPlayerFirst;
	int MaxGametimes;
	int depth = 2;		// Computer_HS alpha-beta search depth

	Parameters Params = new Parameters();
	DecimalFormat df = new DecimalFormat("###.###");


	PlayOthelloGamesAction_vsObserver(HarmonySearch HS, int left, int right,
			Boolean[] isEliteHarmony, Computer COM, int MaxGametimes, Boolean isPlayerFirst, int depth){
		this.HS = HS;
		this.l = left;
		this.r = right;
		this.isEliteHarmony = isEliteHarmony;
		this.COM = COM;
		this.MaxGametimes = MaxGametimes;
		this.isPlayerFirst = isPlayerFirst;
		this.depth = depth;
		if(HS.hms < this.MaxTaskUnitSize) this.MaxTaskUnitSize = HS.hms;
	}

	@Override
	protected void compute() {
		if (this.r - this.l > this.MaxTaskUnitSize) {
			// Divide Task
			int mid = (this.r + this.l) / 2;
			invokeAll(asList(
					new PlayOthelloGamesAction_vsObserver(HS, l, mid,
							this.isEliteHarmony, this.COM, this.MaxGametimes, this.isPlayerFirst, depth),
							new PlayOthelloGamesAction_vsObserver(HS, mid, r,
									this.isEliteHarmony, this.COM, this.MaxGametimes, this.isPlayerFirst, depth)
					) );
		} else {
			// Execute
			for(int k=l;k<r;k++){
				if(this.isEliteHarmony[k] == false) continue;

				// Player1 Setting (New Harmony, k)
				NeuralNetwork NN1 = new NeuralNetwork(
						Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
						Params.hidden_layer_size3, Params.output_layer_size,
						HS.HM1[k], HS.HM2[k], HS.HM3[k], HS.HM4[k]);
				Computer PL1 = new Computer(new Computer_HS(NN1, depth));

				// Player2 Setting (Old Harmony, k2)
				Computer PL2 = COM;

				// Play Game
				PL1.setColor(this.isPlayerFirst ? Params.black : Params.white);
				PL2.setColor(PL1.oppcolor);
				this.VsComputerNtimes(PL1, PL2, k);
			}
		}
	}

	// vs Computer（N試合）
	private void VsComputerNtimes(Computer PL, Computer COM, int k){
		int win = 0, lose = 0, draw = 0;
		for(int i=0;i<this.MaxGametimes;i++){
			Pii res = this.VsComputer(PL, COM);
			if(res.first == res.second) draw++;
			else if (res.first > res.second) win++;
			else lose++;
		}
		System.out.println("No." + k + " (F:" + df.format(HS.F[k]) + ")   "  + "win : " + win + " , draw : " + draw + " , lose : " + lose);
		return;
	}

	// vs Computer（1試合）
	private Pii VsComputer(Computer PL, Computer COM){
		// Game Preprocess
		int player_color   = this.isPlayerFirst ? Params.black : Params.white;
		int computer_color = this.isPlayerFirst ? Params.white : Params.black;
		PL.setColor(player_color);
		COM.setColor(computer_color);

		// Play Game
		Game GameMaster = new Game(PL, COM);
		Pii result = GameMaster.Play();
		return result;
	}
}
