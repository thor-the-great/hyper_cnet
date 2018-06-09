package contentnet.processinglogic;

import contentnet.category.UNSPSCRecord;

import java.util.Set;

public interface IStrategy {
    Set<String> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, UNSPSCRecord categoryRecord);
}
