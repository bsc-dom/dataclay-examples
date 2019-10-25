package consumer;

import es.bsc.compss.types.annotations.task.Method;
import es.bsc.compss.types.annotations.Constraints;
import es.bsc.compss.types.annotations.Parameter;
import es.bsc.compss.types.annotations.parameter.Direction;
import es.bsc.compss.types.annotations.parameter.Type;
import model.chunked.ChunkedText;
import model.TextStats;

public interface WordcountChunkedItf {
	@Method(declaringClass = "consumer.WordcountChunked")
	public TextStats wordCountNewStats(@Parameter(type = Type.OBJECT, direction = Direction.IN) ChunkedText text,
			@Parameter(type = Type.BOOLEAN) boolean persistStats);

	@Method(declaringClass = "consumer.WordcountChunked")
	public TextStats reduceTask(@Parameter(type = Type.OBJECT, direction = Direction.IN) TextStats m1,
			@Parameter(type = Type.OBJECT, direction = Direction.IN) TextStats m2);
	
	@Constraints(computingUnits = "32")
	@Method(declaringClass = "consumer.WordcountChunked")
	void activateTracesAtWorker(
			@Parameter(type = Type.LONG) long syncTime);

	@Constraints(computingUnits = "32")
	@Method(declaringClass = "consumer.WordcountChunked")
	void createTracesAtWorker(
			@Parameter(type = Type.STRING) String toPath);
}
