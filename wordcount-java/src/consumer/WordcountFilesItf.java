package consumer;

import java.util.HashMap;

import es.bsc.compss.types.annotations.task.Method;
import es.bsc.compss.types.annotations.Parameter;
import es.bsc.compss.types.annotations.parameter.Direction;
import es.bsc.compss.types.annotations.parameter.Type;

public interface WordcountFilesItf {

	@Method(declaringClass = "consumer.WordcountFiles")
	public HashMap<String, Integer> wordCount(@Parameter(type = Type.STRING, direction = Direction.IN) String filePath);

	@Method(declaringClass = "consumer.WordcountFiles")
	public HashMap<String, Integer> reduceTask(
			@Parameter(type = Type.OBJECT, direction = Direction.IN) HashMap<String, Integer> m1,
			@Parameter(type = Type.OBJECT, direction = Direction.IN) HashMap<String, Integer> m2);

}
