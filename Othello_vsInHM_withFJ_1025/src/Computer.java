import java.text.DecimalFormat;
import java.util.Random;


public class Computer {
	// 各Computerクラスのラッパークラス的な
	// きれいな書き方が分からない...

	int color;
	int mycolor;
	int oppcolor;

	int empty;
	int black;
	int white;

	int AI_num;					// -1 : Harmony, 0 : Random, 1 : Easy, 2 : Normal
	Computer_Base   COM_AIrandom;
	Computer_Easy   COM_AIeasy;
	Computer_Normal COM_AInormal;
	Computer_HS		COM_HS;

	Random rand = new Random();
	DecimalFormat df = new DecimalFormat("###.##");


	Computer(Computer_HS C){
		this.AI_num = -1;
		this.COM_HS = C;
		this.color = C.color;
		this.mycolor = C.mycolor;
		this.oppcolor = C.oppcolor;
		this.setParmas();
	}
	Computer(Computer_Base C){
		this.AI_num = 0;
		this.COM_AIrandom = C;
		this.color = C.color;
		this.mycolor = C.mycolor;
		this.oppcolor = C.oppcolor;
		this.setParmas();
	}
	Computer(Computer_Easy C){
		this.AI_num = 1;
		this.COM_AIeasy = C;
		this.color = C.color;
		this.mycolor = C.mycolor;
		this.oppcolor = C.oppcolor;
		this.setParmas();
	}
	Computer(Computer_Normal C){
		this.AI_num = 2;
		this.COM_AInormal = C;
		this.color = C.color;
		this.mycolor = C.mycolor;
		this.oppcolor = C.oppcolor;
		this.setParmas();
	}
	private void setParmas(){
		Parameters Params = new Parameters();
		this.empty = Params.empty;
		this.black = Params.black;
		this.white = Params.white;
		return;
	}

	// 色を設定する
	public void setColor(int my_color){
		this.color = my_color;
		this.mycolor = my_color;
		this.oppcolor = my_color * -1;
		switch(this.AI_num){
		case -1:
			this.COM_HS.setColor(my_color);
			break;
		case 0:
			this.COM_AIrandom.setColor(my_color);
			break;
		case 1:
			this.COM_AIeasy.setColor(my_color);
			break;
		case 2:
			this.COM_AInormal.setColor(my_color);
			break;
		default:
			System.out.println("No AI_num Error!(setColor)");
			break;
		}
		return;
	}

	// 次の手を選択する
	public Pii getNextMove_xy(int[][] BD, int turn){
		Pii p = new Pii();
		switch(this.AI_num){
		case -1:
			p = this.COM_HS.getNextMove_xy(BD, turn);
			break;
		case 0:
			p = this.COM_AIrandom.getNextMove_xy(BD);
			break;
		case 1:
			p = this.COM_AIeasy.getNextMove_xy(BD);
			break;
		case 2:
			p = this.COM_AInormal.getNextMove_xy(BD);
			break;
		default:
			System.out.println("No AI_num Error!(getNextMove_xy)");
			p = new Pii(-1, -1);
			break;
		}
		return p;
	}
}
