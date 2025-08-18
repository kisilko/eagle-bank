package com.github.kisilko.eagle_bank.account;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
class BankAccountModelAssembler implements RepresentationModelAssembler<BankAccount, EntityModel<BankAccount>> {

    @Override
    public EntityModel<BankAccount> toModel(BankAccount bankAccount) {
        var controller = BankAccountController.class;

        var selfLink = linkTo(methodOn(controller).accountDetails(bankAccount.getId())).withSelfRel();
//        var updateLink = linkTo(methodOn(controller).updateAccountDetails(user.getId(), null)).withRel("update");
//        var deleteLink = linkTo(methodOn(controller).deleteAccount(user.getId())).withRel("delete");

        return EntityModel.of(bankAccount, selfLink /*, updateLink, deleteLink */);
    }
}
