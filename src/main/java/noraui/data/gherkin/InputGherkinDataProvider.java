package noraui.data.gherkin;

import java.util.ArrayList;
import java.util.Arrays;

import noraui.data.CommonDataProvider;
import noraui.data.DataInputProvider;
import noraui.data.DataProvider;
import noraui.exception.TechnicalException;
import noraui.exception.data.EmptyDataFileContentException;
import noraui.exception.data.WrongDataFileFormatException;
import noraui.gherkin.GherkinFactory;
import noraui.model.Model;

/**
 * This DataInputProvider can be used if you want to provide Gherkin example by
 * your own. Scenario initiator inserts will be skipped.
 *
 * @author nhallouin
 */
public class InputGherkinDataProvider extends CommonDataProvider implements DataInputProvider {
    private String[] examples = new String[] {};

    public InputGherkinDataProvider() {
        logger.info("Input data provider used is GHERKIN");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(String scenario) throws TechnicalException {
        examples = GherkinFactory.getExamples(scenario);
        try {
            initColumns();
        } catch (EmptyDataFileContentException | WrongDataFileFormatException e) {
            logger.error(TechnicalException.TECHNICAL_ERROR_MESSAGE_DATA_IOEXCEPTION, e);
            System.exit(-1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbLines() throws TechnicalException {
        return examples.length;
    }

    /**
     * Gets prepared Gherkin examples.
     *
     * @return an array of examples
     */
    public String[] getExamples() {
        return examples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readValue(String column, int line) throws TechnicalException {
        if (examples.length > 0) {
            String[] lineContent = readLine(line, true);
            int i = columns.indexOf(column) + 1;
            if (i > 0 && null != lineContent && lineContent.length > i) {
                return lineContent[i];
            } else {
                return "";
            }

        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] readLine(int line, boolean readResult) throws TechnicalException {
        if (examples.length > 0 && examples.length > line) {
            String[] lineContent = examples[line].split("\\|", -1);
            if (lineContent.length < 3) {
                throw new TechnicalException(TechnicalException.TECHNICAL_EXPECTED_AT_LEAST_AN_ID_COLUMN_IN_EXAMPLES);
            }
            return Arrays.copyOfRange(lineContent, 2, (readResult) ? lineContent.length + 1 : lineContent.length);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Model> getModel(String modelPackages) throws TechnicalException {
        return null;
    }

    private void initColumns() throws EmptyDataFileContentException, WrongDataFileFormatException {
        columns = new ArrayList<String>();
        if (examples.length > 1) {
            String[] cols = examples[0].split("\\|", -1);
            for (int i = 1; i < cols.length - 1; i++) {
                columns.add(cols[i]);
            }
        } else {
            throw new EmptyDataFileContentException("Input data file is empty or only result column is provided.");
        }
        if (columns.size() < 2) {
            throw new EmptyDataFileContentException("Input data file is empty or only result column is provided.");
        }
        resultColumnName = columns.get(columns.size() - 1);
        if (!isResultColumnNameAuthorized(resultColumnName)) {
            resultColumnName = DataProvider.AUTHORIZED_NAMES_FOR_RESULT_COLUMN.get(0);
        }
    }
}
