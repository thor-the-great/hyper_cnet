package contentnet.processinglogic;

import contentnet.category.UNSPSCRecord;

import java.util.List;
import java.util.Set;

public interface IStrategy {
    Set<List<String>> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, UNSPSCRecord categoryRecord);
}
