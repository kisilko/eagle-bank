package com.github.kisilko.eagle_bank.account;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;

    public BankAccount createAccount(BankAccountCreateRequest bankAccountCreateRequest) {
        BankAccount newBankAccount = modelMapper.map(bankAccountCreateRequest, BankAccount.class);
        return bankAccountRepository.save(newBankAccount);
    }

    public Optional<BankAccount> findById(Long accountId) {
        return bankAccountRepository.findById(accountId);
    }
}
