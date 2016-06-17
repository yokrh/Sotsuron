import static java.util.Arrays.*;

import java.util.Random;
import java.util.concurrent.RecursiveAction;


public class PlayOthelloGamesAction_inHM extends RecursiveAction {
	int MaxTaskUnitSize = 1 ; 	// tasks are split up to this size
	int l, r;					// [l,r)

	HarmonySearch HS;

	int MaxOppNum;
	int MaxGametimes;
	int depth = 2;		// Computer_HS alpha-beta search depth

	Parameters Params = new Parameters();


	PlayOthelloGamesAction_inHM(HarmonySearch HS, int left, int right, int MaxOppNum, int MaxGametimes){
		this.HS = HS;
		this.l = left;
		this.r = right;
		this.MaxOppNum = MaxOppNum;
		this.MaxGametimes = MaxGametimes;
		if(HS.hms < this.MaxTaskUnitSize) this.MaxTaskUnitSize = HS.hms;
	}

	@Override
	protected void compute() {
		if (this.r - this.l > this.MaxTaskUnitSize) {
			// Divide Task
			int mid = (this.r + this.l) / 2;
			invokeAll( asList(
					new PlayOthelloGamesAction_inHM(HS, l, mid, MaxOppNum, MaxGametimes),
					new PlayOthelloGamesAction_inHM(HS, mid, r, MaxOppNum, MaxGametimes)
					) );
		} else {
			// Execute
			for(int k=l;k<r;k++){
				//System.out.print(" " + k + " ");
				//System.out.print(".");
				// Player1 Setting (New Harmony, k)
				NeuralNetwork NN1 = new NeuralNetwork(
						Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
						Params.hidden_layer_size3, Params.output_layer_size,
						HS.HM1[k], HS.HM2[k], HS.HM3[k], HS.HM4[k]);
				Computer PL1 = new Computer(new Computer_HS(NN1, depth));

				for(int k2_=0;k2_<this.MaxOppNum;k2_++){
					int k2 = (k + k2_) % HS.hms;
					if(k == k2) continue;
					for(int t=0;t<this.MaxGametimes;t++){
						// Player2 Setting (Old Harmony, k2)
						NeuralNetwork NN2 = new NeuralNetwork(
								Params.input_layer_size, Params.hidden_layer_size, Params.hidden_layer_size2,
								Params.hidden_layer_size3, Params.output_layer_size,
								HS.HM1[k2], HS.HM2[k2], HS.HM3[k2], HS.HM4[k2]);
						Computer PL2 = new Computer(new Computer_HS(NN2, depth));

						// Play Game
						PL1.setColor(new Random(System.currentTimeMillis()).nextBoolean() ? Params.black : Params.white);
						PL2.setColor(PL1.oppcolor);
						Game GameMaster = new Game(PL1, PL2);
						Pii result = GameMaster.Play();

						// Evaluate F
						HS.F[k]  += HS.CalcF(result);
						HS.F[k2] += HS.CalcF(new Pii(result.second, result.first));
					}
				}
			}
		}
	}
}
