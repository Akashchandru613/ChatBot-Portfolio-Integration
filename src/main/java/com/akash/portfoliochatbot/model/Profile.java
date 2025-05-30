package com.akash.portfoliochatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    public String name;
    public String about;
    public List<String> skills;
    public List<Project> projects;
    public Education education;
    public List<Experience> experience;
    public Contact contact;
    public Hobbies hobbies;

    public static class Project {
        public String name;
        public String description;
        @JsonProperty("technologyUsed")
        public String technologyUsed;
    }

    public static class Education {
        public String degree;
        public String university;
        public double cgpa;
    }

    public static class Experience {
        public String company;
        public String role;
        public String duration;
        public String description;
    }

    public static class Contact {
        public String email;
        public String linkedin;
        public String github;
    }

    public static class Hobbies {
        public String Hobby1;
        public String Hobby2;
        public String Hobby3;
        public String Hobby4;
        public String Hobby5;
        public String Hobby6;
    }
}
