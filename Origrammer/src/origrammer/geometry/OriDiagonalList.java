package origrammer.geometry;

public class OriDiagonalList {

	public int n;
	public OriDiagonal head;
	
	OriDiagonalList() {
		head = null;
		n = 0;
	}
	
	private void initHead(OriDiagonal diag) {
		head = new OriDiagonal(diag.v1, diag.v2);
		head.next = head.prev = head;
		n = 1;
	}
	
	public void clearDiagonalList() {
		if (head != null) {
			head = null;
		}
		n = 0;
	}
	public void insertBeforeHead(OriDiagonal diag) {
		if (head == null) {
			initHead(diag);
		} else {
			insertBefore(diag, head);
		}
	}
	
	private void insertBefore(OriDiagonal newD, OriDiagonal oldD) {
		if (head == null) {
			initHead(newD);
		} else {
			oldD.prev.next = newD;
			newD.prev = oldD.prev;
			newD.next = oldD;
			oldD.prev = newD;
			n++;
		}
	}
	
	  public void printDiagonals() {

	    OriDiagonal temp = head;
	    int i = 0;
	    
	    do {
	      temp.printDiagonal(i);
	      temp = temp.next;
	      i++;
	    } while ( temp != head );

	  }
	
}
