package br.com.datum.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida um CPF através do algoritmo Módulo 11, usado para o cálculo
 * dos dois dígitos verificadores do documento.
 */
public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) {
            return false;
        }

        String digits = cpf.replaceAll("[^0-9]", "");

        if (digits.length() != 11) {
            return false;
        }

        // CPFs com todos os dígitos iguais (ex.: 111.111.111-11) passam
        // matematicamente no Módulo 11, mas não são documentos válidos.
        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        int[] numbers = digits.chars().map(Character::getNumericValue).toArray();

        int firstCheckDigit = calculateCheckDigit(numbers, 9);
        if (firstCheckDigit != numbers[9]) {
            return false;
        }

        int secondCheckDigit = calculateCheckDigit(numbers, 10);
        return secondCheckDigit == numbers[10];
    }

    private int calculateCheckDigit(int[] numbers, int digitsCount) {
        int weight = digitsCount + 1;
        int sum = 0;

        for (int i = 0; i < digitsCount; i++) {
            sum += numbers[i] * weight--;
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
