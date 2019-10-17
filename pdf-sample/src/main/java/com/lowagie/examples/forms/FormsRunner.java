package com.lowagie.examples.forms;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.forms.create.CreateFormsRunner;
import com.lowagie.examples.forms.fill.FillingFormsRunner;

public class FormsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "forms");

        FormCheckbox.main(argsNew);
        FormCombo.main(argsNew);
        FormList.main(argsNew);
        FormPushButton.main(argsNew);
        FormRadioButton.main(argsNew);
        FormSignature.main(argsNew);
        FormTextField.main(argsNew);
        TextFields.main(argsNew);
        ListFields.main(argsNew);
        SimpleRegistrationForm.main(argsNew);

        CreateFormsRunner.main(argsNew);
        FillingFormsRunner.main(argsNew);
    }
}
