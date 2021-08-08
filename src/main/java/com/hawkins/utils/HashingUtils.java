package com.hawkins.utils;

import java.io.IOException;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class HashingUtils {


	public static String md5(PathElement file) {
		try {
			return Files.asByteSource(file.getPath().toFile()).hash(Hashing.md5()).toString();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Failed to calculate md5 of %s", file));
		}
	}	

	public static String shaSum256(PathElement file) {
		try {
			return Files.asByteSource(file.getPath().toFile()).hash(Hashing.sha256()).toString();
		} catch (IOException e) {
			throw new RuntimeException(String.format("Failed to calculate shaSum256 of %s", file));
		}
	}
}
