package com.ardublock.translator.block.hummingbird;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.TranslatorBlock;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

public class Servo extends TranslatorBlock {

	public Servo(Long blockId, Translator translator, String codePrefix,
			String codeSuffix, String label) {
		super(blockId, translator, codePrefix, codeSuffix, label);
	}

	@Override
	public String toCode() throws SocketNullException,
			SubroutineNotDeclaredException {
		TranslatorBlock PortBlock = this.getRequiredTranslatorBlockAtSocket(0);
		TranslatorBlock IntensityBlock = this.getRequiredTranslatorBlockAtSocket(1);
		String PortNum = PortBlock.toCode();
		String IntensityNum = IntensityBlock.toCode();
		translator.addHeaderFile("Hummingbird.h");
		translator.addDefinitionCommand("Hummingbird bird;");
		translator.addSetupCommand("bird.init();");
		String ret = "bird.setServo("+PortNum+","+IntensityNum+");";
		return codePrefix+ret+codeSuffix;
	}

}
