package co.com.pragma.r2dbc.entities;

import co.com.pragma.r2dbc.utils.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class RoleEntity {

    @Id
    @Column("unique_id")
    private Byte uniqueId;

    private RoleType name;

    private String description;
}
