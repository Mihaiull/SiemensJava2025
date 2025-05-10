package com.siemens.internship.Class;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.transform.Source;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item implements Source {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;

    // Add email regex validation
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            // Explanation of the regex:
            // ^ Start of string
            // [a-zA-Z0-9._%+-]+ Matches one or more alphanumeric characters, dots, underscores, percent signs, plus signs, or hyphens
            // @ Matches the @ symbol
            // [a-zA-Z0-9.-]+ Matches one or more alphanumeric characters, dots, or hyphens
            // \\. Matches a literal dot
            // [a-zA-Z]{2,} Matches two or more alphabetic characters (the top-level domain)
            // $ End of string

            flags = {Pattern.Flag.CASE_INSENSITIVE},
            //makes the regex case-insensitive


            message = "Invalid email format"
            // Custom error message
    )
    private String email;

    @Override
    public void setSystemId(String systemId) {
        // No implementation needed for this example
    }

    @Override
    public String getSystemId() {
        return "";
    }
}