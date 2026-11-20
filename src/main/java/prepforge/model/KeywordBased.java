package prepforge.model;

import java.util.List;

/** Implemented by every question that is marked by keyword coverage. */
public interface KeywordBased {
    /** Expected keywords, in lower case. */
    List<String> getKeywords();
}
