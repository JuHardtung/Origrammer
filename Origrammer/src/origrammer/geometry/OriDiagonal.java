package origrammer.geometry;

public class OriDiagonal {
	public OriVertex v1, v2;
	public OriDiagonal next, prev;
	
	public OriDiagonal() {
		next = prev = null;
		v1 = v2 = new OriVertex();
	}
	
	public OriDiagonal(OriVertex v1, OriVertex v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public void printDiagonal(int index) {
		System.out.println("D" + index + " = ");
		v1.printVertex(index);
		v2.printVertex(index);
	}

}
