import static java.util.Arrays.*;

import java.util.Random;
import java.util.concurrent.RecursiveAction;


public class PlayOthelloGamesAction_NewvsOld extends RecursiveAction {
	int MaxTaskUnitSize = 1 ; 	// tasks are split up to this size
	int l, r;					// [l,r)

	HarmonySearch HS;

	int MaxOppNum;
	int MaxGametimes;
	int depth = 2;		// Computer_HS alpha-beta search depth

	Parameters Params = new Parameters();


	PlayOthelloGamesAction_NewvsOld(HarmonySearch HS, int left, int right){
		this.HS = HS;
		this.l = left;
		this.r = right;
		this.MaxOppNum = Params.MaxOppNum;
		this.MaxGametimes = Params.MaxGametimes;
		if(HS.hms < this.MaxTaskUnitSize) this.MaxTaskUnitSize = HS.hms;
	}

	@Override
	protected void compute() {
		if (this.r - this.l > this.MaxTaskUnitSize) {
			// Divide Task
			int mid = (this.r + this.l) / 2;
			invokeAll( asList(
					new PlayOthelloGamesAction_NewvsOld(HS, l, mid),
					new PlayOthelloGamesAction_NewvsOld(HS, mid, r)
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
						HS.NewH1[k], HS.NewH2[k], HS.NewH3[k], HS.NewH4[k]);
				Computer PL1 = new Computer(new Computer_HS(NN1, depth));

				for(int k2_=0;k2_<this.MaxOppNum;k2_++){
					int k2 = (k + k2_) % HS.hms;
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
						HS.NewF[k] += HS.CalcF(result);
						HS.F[k2] += HS.CalcF(new Pii(result.second, result.first));
					}
				}
			}
		}
	}
}
