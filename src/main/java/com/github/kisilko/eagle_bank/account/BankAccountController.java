package com.github.kisilko.eagle_bank.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
class BankAccountController {

    private final BankAccountService bankAccountService;
    private final BankAccountModelAssembler bankAccountModelAssembler;

    @GetMapping("{accountId}")
    public ResponseEntity<EntityModel<BankAccount>> accountDetails(@PathVariable Long accountId) {
        BankAccount bankAccount = bankAccountService.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException(accountId));
        return ResponseEntity.ok(bankAccountModelAssembler.toModel(bankAccount));
    }

    @PostMapping
    public ResponseEntity<EntityModel<BankAccount>> createAccount(@Valid @RequestBody BankAccountCreateRequest bankAccountCreateRequest) {
        BankAccount newBankAccount = bankAccountService.createAccount(bankAccountCreateRequest);
        EntityModel<BankAccount> accountModel = bankAccountModelAssembler.toModel(newBankAccount);
        return ResponseEntity.created(accountModel.getRequiredLink("self").toUri())
                .body(accountModel);
    }
}
