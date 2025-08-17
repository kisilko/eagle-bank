package com.github.kisilko.eagle_bank.users;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
class UsersModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User entity) {
        var controller = UsersController.class;
        var one = linkTo(methodOn(controller).userDetails(entity.getId())).withSelfRel();
        return EntityModel.of(entity, one);
    }
}
