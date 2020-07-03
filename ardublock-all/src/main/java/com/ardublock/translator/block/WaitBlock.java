
package com.ardublock.translator.block;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

public class WaitBlock extends TranslatorBlock
{

	public WaitBlock(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label)
	{
		super(blockId, translator);
	}

	@Override
	public String toCode() throws SocketNullException, SubroutineNotDeclaredException
	{
		String ret = "\tdelay( ";
		
		TranslatorBlock translatorBlock = this.getRequiredTranslatorBlockAtSocket(0);
		
		//- take the input and multiply it by 1000
		double dbl = Double.parseDouble(translatorBlock.toCode());
		dbl = dbl * 1000;
		
		ret = ret + String.valueOf(dbl) + " );\n";
		return ret;
	}

}
