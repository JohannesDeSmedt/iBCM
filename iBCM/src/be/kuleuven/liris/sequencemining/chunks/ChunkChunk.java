package be.kuleuven.liris.sequencemining.chunks;

import java.util.Collection;
import java.util.HashSet;

public class ChunkChunk extends Chunk{

	private Collection<ClassChunk> chunks = new HashSet<ClassChunk>();
	
	public void addChunk(ClassChunk toAdd){
		chunks.add(toAdd);
	}
	
	public ClassChunk getChunkOfLabel(int i){
		for(ClassChunk c: chunks)
			if(c.getLabel()==i)
				return c;
		return null;		
	}
	
	public Collection<ClassChunk> getAllChunks(){
		return chunks;
	}
	
}
