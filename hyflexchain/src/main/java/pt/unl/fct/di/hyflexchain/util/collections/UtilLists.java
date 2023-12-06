package pt.unl.fct.di.hyflexchain.util.collections;

import java.util.List;

public class UtilLists
{
	public static <T> List<T> subListLastElems(List<T> list, int last)
	{
		int size = list.size();
		int toIndex = size;
		int fromIndex = size - last;
		
		if (fromIndex < 0)
		{
			fromIndex = 0;
		}

		return list.subList(fromIndex, toIndex);
	}
}
