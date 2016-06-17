import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class PlayOthelloGames {
	// オセロの複数試合の管理をするクラス
	// ForkJoin あり

	HarmonySearch HS;
	Parameters Params = new Parameters();
	final int MaxProcessorNum = 64;				// プロセッサ数


	PlayOthelloGames(HarmonySearch HS){
		this.HS = HS;
	}

	// 試合（HM内リーグ戦、Old vs New）
	public void PlayLeagueOldvsNew(){
		System.out.println(" [ New vs Old ] ");
		this.BeforeLeaguePlay();

		//System.out.println("Number of processor available: " + Runtime.getRuntime().availableProcessors());
		ForkJoinPool fjpool = new ForkJoinPool(this.MaxProcessorNum);
		RecursiveAction task = new PlayOthelloGamesAction_NewvsOld(HS, 0, HS.hms);
		fjpool.invoke(task);
		System.out.println();
		//System.out.println("Number of steals: " + fjpool.getStealCount());

		this.AfterLeaguePlay();
		return;
	}

	// 試合（HM内リーグ戦、現世代全て）
	public void PlayLeagueInHM(){
		System.out.println(" [ vs inHM ] ");
		int MaxOppNum = Params.ModifyF_MaxOppNum;
		int MaxGametimes = Params.ModifyF_MaxGametimes;

		this.BeforeLeaguePlay();

		//System.out.println("Number of processor available: " + Runtime.getRuntime().availableProcessors());
		ForkJoinPool fjpool = new ForkJoinPool(this.MaxProcessorNum);
		RecursiveAction task = new PlayOthelloGamesAction_inHM(HS, 0, HS.hms, MaxOppNum + 1, MaxGametimes / 2);
		fjpool.invoke(task);
		System.out.println();
		//System.out.println("Number of steals: " + fjpool.getStealCount());

		this.AfterLeaguePlay(MaxOppNum, MaxGametimes);
		return;
	}

	// 試合（vsObserver）
	// 上位のHarmonyのみ
	public void PlayVsObserver(){
		final int n = 3;
		int MaxGametimes = 50;
		System.out.println(" [ The Best" + n + " Harmony vs Observer ]");

		int best[] = new int[n];
		for(int k=0;k<best.length;k++){
			best[k] = 0;
			for(int i=1;i<this.HS.hms;i++){
				Boolean flg = false;
				for(int j=0;j<k && !flg;j++) if(i == best[j]) flg = true;
				if(flg) continue;
				if(this.HS.F[i] > this.HS.F[best[k]]) best[k] = i;
			}
		}

		Boolean[] isEliteHarmony = new Boolean[HS.hms];
		for(int k=0;k<isEliteHarmony.length;k++) isEliteHarmony[k] = false;
		for(int i=0;i<best.length;i++) isEliteHarmony[best[i]] = true;

		//System.out.println("Number of processor available: " + Runtime.getRuntime().availableProcessors());
		ForkJoinPool fjpool = new ForkJoinPool(this.MaxProcessorNum);
		RecursiveAction task;

		for(int depth=1;depth<=2;depth++){
			System.out.println("Alpha-beta Search depth : " + depth);

			System.out.println("vs Easy ");
			System.out.println("P : Black");
			task = new PlayOthelloGamesAction_vsObserver(HS, 0, HS.hms, isEliteHarmony,
					new Computer(new Computer_Easy()), MaxGametimes, true, depth);
			fjpool.invoke(task);
			System.out.println("P : White");
			task = new PlayOthelloGamesAction_vsObserver(HS, 0, HS.hms, isEliteHarmony,
					new Computer(new Computer_Easy()), MaxGametimes, false, depth);
			fjpool.invoke(task);

			System.out.println("vs Normal ");
			System.out.println("P : Black");
			task = new PlayOthelloGamesAction_vsObserver(HS, 0, HS.hms, isEliteHarmony,
					new Computer(new Computer_Normal()), MaxGametimes, true, depth);
			fjpool.invoke(task);
			System.out.println("P : White");
			task = new PlayOthelloGamesAction_vsObserver(HS, 0, HS.hms, isEliteHarmony,
					new Computer(new Computer_Normal()), MaxGametimes, false, depth);
			fjpool.invoke(task);
		}

		//System.out.println("Number of steals: " + fjpool.getStealCount());
		return;
	}

	// リーグ試合前処理
	private void BeforeLeaguePlay(){
		this.InitializeF();
		return;
	}
	// リーグ試合後処理
	private void AfterLeaguePlay(){
		this.NormalizeF();
		return;
	}
	private void AfterLeaguePlay(int MaxOppNum, int MaxGametimes){
		this.NormalizeF(MaxOppNum, MaxGametimes);
		return;
	}

	// F初期化
	private void InitializeF(){
		for(int k=0;k<this.HS.hms;k++) this.HS.F[k] = 0.0;
		for(int k=0;k<this.HS.hms;k++) this.HS.NewF[k] = 0.0;
		return;
	}
	// F正規化
	private void NormalizeF(){
		for(int k=0;k<this.HS.hms;k++) this.HS.F[k] /= 1.0 * Params.MaxOppNum * Params.MaxGametimes;
		for(int k=0;k<this.HS.hms;k++) this.HS.NewF[k] /= 1.0 * Params.MaxOppNum * Params.MaxGametimes;
		return;
	}
	private void NormalizeF(int MaxOppNum, int MaxGametimes){
		for(int k=0;k<this.HS.hms;k++) this.HS.F[k] /= 1.0 * MaxOppNum * MaxGametimes;
	}
}
