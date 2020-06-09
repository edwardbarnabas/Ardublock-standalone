package com.ardublock.translator.block.hummingbird;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.TranslatorBlock;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

public class TriLEDBlock extends TranslatorBlock {

	public TriLEDBlock(Long blockId, Translator translator, String codePrefix,
			String codeSuffix, String label) {
		super(blockId, translator, codePrefix, codeSuffix, label);
	}

	@Override
	public String toCode() throws SocketNullException,
			SubroutineNotDeclaredException {
		TranslatorBlock PortBlock = this.getRequiredTranslatorBlockAtSocket(0);
		TranslatorBlock RedBlock = this.getRequiredTranslatorBlockAtSocket(1);
		TranslatorBlock GreenBlock = this.getRequiredTranslatorBlockAtSocket(2);
		TranslatorBlock BlueBlock = this.getRequiredTranslatorBlockAtSocket(3);
		String PortNum = PortBlock.toCode();
		String RedNum = RedBlock.toCode();
		String GreenNum = GreenBlock.toCode();
		String BlueNum = BlueBlock.toCode();
		translator.addHeaderFile("Hummingbird.h");
		translator.addDefinitionCommand("Hummingbird bird;");
		translator.addSetupCommand("bird.init();");
		String ret = "bird.setTriColorLED("+PortNum+","+RedNum+","+GreenNum+","+BlueNum+");";
		return codePrefix+ret+codeSuffix;
	}

}
