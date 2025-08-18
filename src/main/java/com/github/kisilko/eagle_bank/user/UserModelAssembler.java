package com.github.kisilko.eagle_bank.user;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User user) {
        var controller = UserController.class;

        var selfLink = linkTo(methodOn(controller).userDetails(user.getId())).withSelfRel();
        var updateLink = linkTo(methodOn(controller).updateUserDetails(user.getId(), null)).withRel("update");
        var deleteLink = linkTo(methodOn(controller).deleteUser(user.getId())).withRel("delete");

        return EntityModel.of(user, selfLink, updateLink, deleteLink);
    }
}
