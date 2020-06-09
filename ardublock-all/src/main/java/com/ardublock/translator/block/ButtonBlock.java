package com.ardublock.translator.block;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

public class ButtonBlock extends TranslatorBlock
{
	public ButtonBlock(Long blockId, Translator translator, String codePrefix,	String codeSuffix, String label)
	{
		super(blockId, translator, codePrefix, codeSuffix, label);
	}

  @Override
	public String toCode() throws SocketNullException, SubroutineNotDeclaredException
	{
		TranslatorBlock pinBlock = this.getRequiredTranslatorBlockAtSocket(0);
    NumberBlock pinNumberBlock = (NumberBlock)pinBlock;

    String pinNumber = pinNumberBlock.toCode();
    translator.addSetupCommand("pinMode(" + pinNumber + ", INPUT_PULLUP);");

		TranslatorBlock buttonPress = this.getRequiredTranslatorBlockAtSocket(1);


    String ret = "\tdigitalRead(" + pinBlock.toCode() + ") == " + buttonPress.toCode() + " ";
		return ret;
	}

}
